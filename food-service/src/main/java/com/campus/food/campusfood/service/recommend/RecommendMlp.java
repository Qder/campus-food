package com.campus.food.campusfood.service.recommend;

import com.campus.food.campusfood.dto.FoodRatingPair;
import com.campus.food.campusfood.service.util.FoodRecommendScorer;
import com.campus.food.campusfood.vo.FoodItemVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 课设用小 MLP：用户/菜品 embedding 与协同分、口味命中、均分、销量拼接后打分。
 *
 * <p>输入 20 维 = 用户 8 + 菜品 8 + 额外 4（cf、tagHit、均分、销量）。
 * 隐层 8 维 ReLU，输出 Sigmoid。评价对上 SGD + MSE。样本少于 {@link #MIN_SAMPLES} 时 {@link #isReady()} 为 false。
 *
 * @author yang
 * @since 2026-11-08
 */
public final class RecommendMlp {

    /** 用户、菜品 embedding 维度 */
    public static final int EMB = 8;

    /** 额外特征：cfScore、tagHit、avgScoreNorm、salesNorm */
    public static final int EXTRA = 4;

    /** 输入维度 */
    public static final int INPUT = EMB + EMB + EXTRA;

    /** 隐层宽度 */
    public static final int HIDDEN = 8;

    /** 少于此条评价则不训练 */
    public static final int MIN_SAMPLES = 4;

    private static final int EPOCHS = 40;
    private static final double LEARNING_RATE = 0.05;
    private static final double EMB_INIT = 0.05;

    private final Map<Long, double[]> userEmb = new HashMap<>();
    private final Map<Long, double[]> itemEmb = new HashMap<>();
    private final double[][] w1 = new double[HIDDEN][INPUT];
    private final double[] b1 = new double[HIDDEN];
    private final double[] w2 = new double[HIDDEN];
    private double b2;
    private boolean ready;

    /**
     * 是否已训好，调用方据此决定走 MLP 还是 CF/规则。
     *
     * @return 样本足够且已完成 SGD 时为 true
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * 用评价对训练。额外特征里训练期 tagHit 为 0，cf 用该用户其它评价的均分，均分/销量来自菜品。
     *
     * @param pairs    可见评价
     * @param items    菜品 ID → 候选/库存信息，用于均分和销量
     * @param maxSales 销量归一化分母，至少为 1
     */
    public void train(List<FoodRatingPair> pairs, Map<Long, FoodItemVO> items, int maxSales) {
        ready = false;
        userEmb.clear();
        itemEmb.clear();
        if (pairs == null) {
            return;
        }
        List<FoodRatingPair> samples = new ArrayList<>();
        for (FoodRatingPair pair : pairs) {
            if (pair == null || pair.getUserId() == null || pair.getFoodId() == null) {
                continue;
            }
            if (RecommendSignals.reviewToImplicit(pair.getScore()) <= 0) {
                continue;
            }
            samples.add(pair);
        }
        if (samples.size() < MIN_SAMPLES) {
            return;
        }

        Map<Long, Double> userMean = meanRatingByUser(samples);
        Random rng = new Random(42);
        initWeights(rng);
        for (FoodRatingPair pair : samples) {
            ensureEmb(userEmb, pair.getUserId(), rng);
            ensureEmb(itemEmb, pair.getFoodId(), rng);
        }

        int cap = Math.max(maxSales, 1);
        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            for (FoodRatingPair pair : samples) {
                double[] extra = trainingExtra(pair, items, userMean, cap);
                double target = RecommendSignals.reviewToImplicit(pair.getScore());
                sgdStep(pair.getUserId(), pair.getFoodId(), extra, target);
            }
        }
        ready = true;
    }

    /**
     * 前向打分，结果落在 0~1。未训练时返回 0。
     *
     * @param userId 用户
     * @param foodId 菜品
     * @param extra  长度 {@link #EXTRA}：cf、tagHit、均分、销量；可为 null 视为全 0
     * @return Sigmoid 输出
     */
    public double predict(Long userId, Long foodId, double[] extra) {
        if (!ready || userId == null || foodId == null) {
            return 0;
        }
        double[] x = buildInput(userId, foodId, extra);
        double[] hidden = new double[HIDDEN];
        forwardHidden(x, hidden);
        return sigmoid(dot(w2, hidden) + b2);
    }

    /**
     * 把四个额外特征收成数组，缺省按 0。
     *
     * @param cfScore        协同过滤分
     * @param tagHit         口味命中率
     * @param avgScoreNorm   均分 0~1
     * @param salesNorm      销量 0~1
     * @return 长度 4 的特征
     */
    public static double[] extraOf(double cfScore, double tagHit, double avgScoreNorm, double salesNorm) {
        return new double[] {cfScore, tagHit, avgScoreNorm, salesNorm};
    }

    private static double[] trainingExtra(FoodRatingPair pair,
                                          Map<Long, FoodItemVO> items,
                                          Map<Long, Double> userMean,
                                          int maxSales) {
        FoodItemVO item = items == null ? null : items.get(pair.getFoodId());
        double avg = FoodRecommendScorer.normalizedAvgScore(item == null ? null : item.getAvgScore());
        double sales = FoodRecommendScorer.normalizedSales(item == null ? null : item.getSalesCount(), maxSales);
        double cf = 0;
        if (userMean != null && pair.getUserId() != null && userMean.get(pair.getUserId()) != null) {
            cf = userMean.get(pair.getUserId());
        }
        return extraOf(cf, 0, avg, sales);
    }

    private static Map<Long, Double> meanRatingByUser(List<FoodRatingPair> samples) {
        Map<Long, Double> sum = new HashMap<>();
        Map<Long, Integer> count = new HashMap<>();
        for (FoodRatingPair pair : samples) {
            double v = RecommendSignals.reviewToImplicit(pair.getScore());
            Double old = sum.get(pair.getUserId());
            sum.put(pair.getUserId(), old == null ? v : old + v);
            Integer n = count.get(pair.getUserId());
            count.put(pair.getUserId(), n == null ? 1 : n + 1);
        }
        Map<Long, Double> mean = new HashMap<>();
        for (Map.Entry<Long, Double> e : sum.entrySet()) {
            int n = count.get(e.getKey());
            mean.put(e.getKey(), e.getValue() / n);
        }
        return mean;
    }

    private void sgdStep(Long userId, Long foodId, double[] extra, double target) {
        double[] x = buildInput(userId, foodId, extra);
        double[] z1 = new double[HIDDEN];
        double[] hidden = new double[HIDDEN];
        for (int j = 0; j < HIDDEN; j++) {
            double s = b1[j];
            for (int i = 0; i < INPUT; i++) {
                s += w1[j][i] * x[i];
            }
            z1[j] = s;
            hidden[j] = relu(s);
        }
        double z2 = dot(w2, hidden) + b2;
        double y = sigmoid(z2);
        double dZ2 = (y - target) * y * (1 - y);

        double[] dHidden = new double[HIDDEN];
        for (int j = 0; j < HIDDEN; j++) {
            dHidden[j] = dZ2 * w2[j];
            w2[j] -= LEARNING_RATE * dZ2 * hidden[j];
        }
        b2 -= LEARNING_RATE * dZ2;

        double[] dX = new double[INPUT];
        for (int j = 0; j < HIDDEN; j++) {
            double dZ1 = z1[j] > 0 ? dHidden[j] : 0;
            b1[j] -= LEARNING_RATE * dZ1;
            for (int i = 0; i < INPUT; i++) {
                dX[i] += dZ1 * w1[j][i];
                w1[j][i] -= LEARNING_RATE * dZ1 * x[i];
            }
        }

        addEmbGrad(userEmb.get(userId), dX, 0);
        addEmbGrad(itemEmb.get(foodId), dX, EMB);
    }

    private void addEmbGrad(double[] emb, double[] dX, int offset) {
        if (emb == null) {
            return;
        }
        for (int i = 0; i < EMB; i++) {
            emb[i] -= LEARNING_RATE * dX[offset + i];
            if (emb[i] > 1) {
                emb[i] = 1;
            } else if (emb[i] < -1) {
                emb[i] = -1;
            }
        }
    }

    private double[] buildInput(Long userId, Long foodId, double[] extra) {
        double[] x = new double[INPUT];
        copyEmb(userEmb.get(userId), x, 0);
        copyEmb(itemEmb.get(foodId), x, EMB);
        if (extra != null) {
            int n = Math.min(EXTRA, extra.length);
            for (int i = 0; i < n; i++) {
                x[EMB + EMB + i] = extra[i];
            }
        }
        return x;
    }

    private static void copyEmb(double[] emb, double[] dest, int offset) {
        if (emb == null) {
            return;
        }
        System.arraycopy(emb, 0, dest, offset, EMB);
    }

    private void forwardHidden(double[] x, double[] hidden) {
        for (int j = 0; j < HIDDEN; j++) {
            double s = b1[j];
            for (int i = 0; i < INPUT; i++) {
                s += w1[j][i] * x[i];
            }
            hidden[j] = relu(s);
        }
    }

    private void initWeights(Random rng) {
        double scale = Math.sqrt(6.0 / (INPUT + HIDDEN));
        for (int j = 0; j < HIDDEN; j++) {
            b1[j] = 0;
            w2[j] = (rng.nextDouble() * 2 - 1) * scale;
            for (int i = 0; i < INPUT; i++) {
                w1[j][i] = (rng.nextDouble() * 2 - 1) * scale;
            }
        }
        b2 = 0;
    }

    private void ensureEmb(Map<Long, double[]> table, Long id, Random rng) {
        if (table.containsKey(id)) {
            return;
        }
        double[] v = new double[EMB];
        for (int i = 0; i < EMB; i++) {
            v[i] = (rng.nextDouble() * 2 - 1) * EMB_INIT;
        }
        table.put(id, v);
    }

    private static double relu(double v) {
        return v > 0 ? v : 0;
    }

    private static double sigmoid(double z) {
        if (z > 20) {
            return 1;
        }
        if (z < -20) {
            return 0;
        }
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private static double dot(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
        }
        return s;
    }
}

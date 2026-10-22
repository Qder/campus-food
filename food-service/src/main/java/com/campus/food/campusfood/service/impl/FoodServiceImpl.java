package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.entity.FoodCategory;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserFeignClient;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.FoodCategoryMapper;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.service.FoodService;
import com.campus.food.campusfood.service.util.FoodRecommendScorer;
import com.campus.food.campusfood.util.TagTextUtil;
import com.campus.food.campusfood.vo.FoodCategoryVO;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodRecommendVO;
import com.campus.food.campusfood.vo.PageVO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 美食浏览与推荐实现。
 *
 * <p>
 * 推荐走课设规则打分：由 {@link FoodRecommendScorer} 加权后排序，并用 {@link TagTextUtil} 过滤忌口。
 *
 * @author yang
 * @since 2026-10-16
 */
@Service
public class FoodServiceImpl implements FoodService {

    /** 默认页码 */
    private static final int DEFAULT_PAGE = 1;
    /** 默认每页条数 */
    private static final int DEFAULT_SIZE = 10;
    /** 每页条数上限 */
    private static final int MAX_SIZE = 50;
    /** 默认推荐条数 */
    private static final int DEFAULT_RECOMMEND_LIMIT = 10;
    /** 候选池上限，课设体量内全量打分后再截断 TopN */
    private static final int RECOMMEND_CANDIDATE_CAP = 200;

    @Autowired
    private FoodCategoryMapper foodCategoryMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     *
     * <p>
     * 按 {@code parentId} 组装树，同级按排序号、ID 升序。
     */
    @Override
    public List<FoodCategoryVO> listCategories() {
        List<FoodCategory> rows = foodCategoryMapper.selectEnabled();

        Map<Integer, FoodCategoryVO> byId = new LinkedHashMap<>();
        for (FoodCategory row : rows) {
            FoodCategoryVO vo = new FoodCategoryVO();
            vo.setId(row.getId());
            vo.setParentId(row.getParentId());
            vo.setName(row.getName());
            vo.setSortNo(row.getSortNo());
            byId.put(row.getId(), vo);
        }

        List<FoodCategoryVO> roots = new ArrayList<>();
        for (FoodCategoryVO vo : byId.values()) {
            Integer parentId = vo.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(vo);
                continue;
            }
            FoodCategoryVO parent = byId.get(parentId);
            if (parent == null) {
                // 父分类已停用：把该节点提升为根，避免整棵子树丢失
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        sortTree(roots);
        return roots;
    }

    /** {@inheritDoc} */
    @Override
    public PageVO<FoodItemVO> listFoods(Integer categoryId, String campusArea, String keyword,
            Integer page, Integer size) {
        int p = page == null || page < 1 ? DEFAULT_PAGE : page;
        int s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        // 空白当成未传。Mapper 只在非 null 时加区域或关键词条件
        String area = trimToNull(campusArea);
        String key = trimToNull(keyword);

        // 传了 keyword 但去空白后为空，说明是无效搜索条件，直接报错
        if (keyword != null && key == null) {
            throw new BizException("搜索关键词不能为空");
        }

        String redisKey = (key == null)
                ? "food:hot:cat:" + (categoryId == null ? "all" : categoryId)
                        + ":area:" + (area == null ? "all" : area)
                        + ":p" + p + ":s" + s
                : "food:search:" + key + ":p" + p + ":s" + s;

        try {
            String json = stringRedisTemplate.opsForValue().get(redisKey);
            if (json != null && !json.isBlank()) {
                // 反序列化，将json转换为PageVO<FoodItemVO>对象
                return objectMapper.readValue(json,
                        objectMapper.getTypeFactory().constructParametricType(PageVO.class, FoodItemVO.class));
            }
        } catch (Exception ignored) {
            // Redis 挂了就走下面哪一步使用数据库查询数据
        }

        int total = foodItemMapper.countOnShelf(categoryId, area, key);
        List<FoodItemVO> list = total == 0
                ? List.of()
                : foodItemMapper.selectOnShelf(categoryId, area, key, (p - 1) * s, s);

        PageVO<FoodItemVO> vo = new PageVO<>();
        vo.setTotal(total);
        vo.setPage(p);
        vo.setSize(s);
        vo.setList(list);

        // 如果查询到数据，则将数据缓存到Redis中，防止存入空数据
        if (total > 0) {
            try {
                // 设置缓存过期时间，有关键词的缓存时间更短
                java.time.Duration ttl = (key == null)
                        ? java.time.Duration.ofMinutes(10)
                        : java.time.Duration.ofMinutes(5);
                // 缓存写入（回填 Redis）
                stringRedisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(vo), ttl);
            } catch (Exception ignored) {
            }
        }

        return vo;
    }

    /** {@inheritDoc} */
    @Override
    public FoodItemVO getById(Long id) {
        if (id == null) {
            throw new BizException("美食不存在");
        }
        // 按照id查询美食
        FoodItemVO vo = foodItemMapper.selectOnShelfById(id);
        // 如果美食不存在或未上架，则抛出异常
        if (vo == null) {
            throw new BizException("美食不存在或未上架");
        }
        // 获取当前用户id
        Long userId = UserContext.getUserId();

        if (userId != null) {
            try {
                // 构建redis key
                String browseKey = "food:browse:" + userId;
                // 将美食id添加到redis列表中
                stringRedisTemplate.opsForList().leftPush(browseKey, String.valueOf(id));
                // 修剪列表，只保留前50条记录，防止列表过大
                stringRedisTemplate.opsForList().trim(browseKey, 0, 49);
            } catch (Exception ignored) {
                // Redis 挂了就走下面哪一步使用数据库查询数据
            }
        }
        return vo;
    }

    /** {@inheritDoc} */
    @Override
    public List<FoodRecommendVO> recommend(Long userId, Integer limit) {
        int topN = limit == null || limit < 1 ? DEFAULT_RECOMMEND_LIMIT : Math.min(limit, MAX_SIZE);

        List<FoodItemVO> candidates = foodItemMapper.selectOnShelfForRecommend(RECOMMEND_CANDIDATE_CAP);
        if (candidates.isEmpty()) {
            return List.of();
        }

        // 销量归一化的分母：取候选池最高销量，缺失或全为 0 时兜底为 1
        int maxSales = candidates.stream()
                .map(FoodItemVO::getSalesCount)
                .filter(v -> v != null && v > 0)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1);

        if (userId == null) {
            throw new BizException("请先登录后再获取推荐");
        }
        UserInfo user = userFeignClient.getUserInfo(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        // 没有口味偏好就无法个性化，直接提示完善
        List<String> userTastes = TagTextUtil.parseTags(user.getTasteTags());
        if (userTastes.isEmpty()) {
            throw new BizException("请先完善口味偏好");
        }
        Set<String> dislikes = TagTextUtil.parseTagSet(user.getDislikeIngredients());

        List<FoodRecommendVO> scored = new ArrayList<>();
        for (FoodItemVO row : candidates) {
            // 命中忌口食材的候选直接剔除
            if (TagTextUtil.containsAnyTag(TagTextUtil.parseTagSet(row.getIngredients()), dislikes)) {
                continue;
            }

            FoodItem food = toScoringItem(row);
            double rawScore = FoodRecommendScorer.scorePersonalized(food, userTastes, maxSales);

            FoodRecommendVO vo = new FoodRecommendVO();
            vo.setFood(stripRecommendOnlyFields(row));
            vo.setScore(FoodRecommendScorer.roundScore(rawScore));
            vo.setRecommendTags(TagTextUtil.joinTags(
                    FoodRecommendScorer.matchedRecommendTags(userTastes, food)));
            scored.add(vo);
        }

        // 同分依次比销量、比 ID，保证结果稳定可复现
        scored.sort(Comparator
                .comparing(FoodRecommendVO::getScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(v -> v.getFood().getSalesCount(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(v -> v.getFood().getId(), Comparator.nullsLast(Comparator.reverseOrder())));

        if (scored.size() <= topN) {
            return scored;
        }
        return new ArrayList<>(scored.subList(0, topN));
    }

    /**
     * 把展示用 VO 转成打分所需的最小实体。
     *
     * @param row 候选美食 VO
     * @return 仅含打分字段的 {@link FoodItem}
     */
    private FoodItem toScoringItem(FoodItemVO row) {
        FoodItem item = new FoodItem();
        item.setId(row.getId());
        item.setFoodType(row.getFoodType());
        item.setTasteTags(row.getTasteTags());
        item.setIngredients(row.getIngredients());
        item.setAvgScore(row.getAvgScore());
        item.setSalesCount(row.getSalesCount());
        return item;
    }

    /**
     * 响应里去掉仅用于计算的口味/食材字段。
     *
     * @param row 候选美食 VO
     * @return 可安全返回给前端的 VO 副本
     */
    private FoodItemVO stripRecommendOnlyFields(FoodItemVO row) {
        FoodItemVO out = new FoodItemVO();
        out.setId(row.getId());
        out.setMerchantId(row.getMerchantId());
        out.setShopName(row.getShopName());
        out.setCampusArea(row.getCampusArea());
        out.setCategoryId(row.getCategoryId());
        out.setName(row.getName());
        out.setFoodType(row.getFoodType());
        out.setPrice(row.getPrice());
        out.setStock(row.getStock());
        out.setDescription(row.getDescription());
        out.setCoverUrl(row.getCoverUrl());
        out.setAvgScore(row.getAvgScore());
        out.setReviewCount(row.getReviewCount());
        out.setSalesCount(row.getSalesCount());
        out.setRecommendTag(row.getRecommendTag());
        out.setStatus(row.getStatus());
        return out;
    }

    /**
     * 递归对分类树按排序号、ID 升序排列。
     *
     * @param nodes 待排序的同级节点
     */
    private void sortTree(List<FoodCategoryVO> nodes) {
        nodes.sort(Comparator.comparing(FoodCategoryVO::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(FoodCategoryVO::getId, Comparator.nullsLast(Integer::compareTo)));
        for (FoodCategoryVO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    /**
     * 去首尾空白。
     *
     * @param s 原始字符串，可为 null
     * @return 去空白后的字符串；空串返回 null
     */
    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

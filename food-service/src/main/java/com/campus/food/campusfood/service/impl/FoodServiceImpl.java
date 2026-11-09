package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.entity.FoodCategory;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.FoodCategoryMapper;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.service.FoodService;
import com.campus.food.campusfood.service.recommend.RecommendService;
import com.campus.food.campusfood.service.recommend.RecommendSignals;
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

/**
 * 美食浏览与推荐实现。
 *
 * <p>列表与搜索采用 Redis Cache-Aside；登录用户查看详情时写入浏览历史 List。
 * 推荐委托 {@link RecommendService}：协同过滤 + MLP，冷启动回退规则或热门。
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
    /** 无关键词列表缓存 TTL，单位：分钟 */
    private static final int HOT_LIST_CACHE_MINUTES = 10;
    /** 关键词搜索缓存 TTL，单位：分钟 */
    private static final int SEARCH_CACHE_MINUTES = 5;
    /** 单用户浏览历史 List 最大保留条数（含本次写入后 trim 的下标上界） */
    private static final int BROWSE_HISTORY_MAX = 50;

    @Autowired
    private FoodCategoryMapper foodCategoryMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private RecommendService recommendService;

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

    /**
     * {@inheritDoc}
     *
     * <p>Cache-Aside：先读 Redis（热门 key 或搜索 key），未命中再查库；{@code total > 0} 时回填缓存。
     * Redis 异常时降级为直查 MySQL，不影响接口可用性。空白关键词入参视为非法搜索条件。
     */
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
                        ? java.time.Duration.ofMinutes(HOT_LIST_CACHE_MINUTES)
                        : java.time.Duration.ofMinutes(SEARCH_CACHE_MINUTES);
                // 缓存写入（回填 Redis）
                stringRedisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(vo), ttl);
            } catch (Exception ignored) {
            }
        }

        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>已登录时异步副作用：{@code LPUSH} + {@code LTRIM} 记录浏览历史，key 为 {@code food:browse:{userId}}。
     * Redis 失败仍返回详情，不因写历史失败阻断主流程。
     */
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
                String browseKey = RecommendSignals.browseKey(userId);
                // 将美食id添加到redis列表中
                stringRedisTemplate.opsForList().leftPush(browseKey, String.valueOf(id));
                // 修剪列表，只保留前50条记录，防止列表过大
                stringRedisTemplate.opsForList().trim(browseKey, 0, BROWSE_HISTORY_MAX - 1);
            } catch (Exception ignored) {
                // Redis 挂了就走下面哪一步使用数据库查询数据
            }
        }
        return vo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FoodRecommendVO> recommend(Long userId, Integer limit) {
        if (userId == null) {
            throw new BizException("请先登录后再获取推荐");
        }
        int topN = limit == null || limit < 1 ? DEFAULT_RECOMMEND_LIMIT : Math.min(limit, MAX_SIZE);
        return recommendService.recommend(userId, topN, RECOMMEND_CANDIDATE_CAP);
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

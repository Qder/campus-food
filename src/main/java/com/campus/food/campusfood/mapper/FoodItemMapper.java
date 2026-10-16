package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.vo.FoodItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * {@code food_item} MyBatis Mapper
 *
 * <p>常用查询键（来自 UNIQUE/INDEX，仅注释提示，不自动生成方法）：
 * - UNIQUE PRIMARY: id → 建议 selectById
 * - INDEX idx_food_merchant: merchant_id → 建议 selectByMerchantId
 * - INDEX idx_food_category: category_id → 建议 selectByCategoryId
 * - INDEX idx_food_status_score: status, avg_score → 建议 selectByStatusAndAvgScore
 * - INDEX idx_food_name: name → 建议 selectByName
 * - INDEX (anonymous): name, description → 建议 selectByNameAndDescription
 *
 * @author yang
 * @since 2026-10-16
 */
@Mapper
public interface FoodItemMapper {

    /**
     * 新增美食。
     *
     * @param entity 待插入的美食
     * @return 影响行数
     */
    int insert(FoodItem entity);

    /**
     * 按主键查询美食（不区分状态）。
     *
     * @param id 美食 ID
     * @return 美食记录；不存在返回 null
     */
    FoodItem selectById(@Param("id") Long id);

    /**
     * 统计上架美食数。
     *
     * @param categoryId 分类 ID，可为 null
     * @param campusArea 校区/区域，可为 null
     * @param keyword    名称/描述关键词，可为 null
     * @return 命中条数
     */
    int countOnShelf(@Param("categoryId") Integer categoryId,
                     @Param("campusArea") String campusArea,
                     @Param("keyword") String keyword);

    /**
     * 分页查询上架美食。
     *
     * @param categoryId 分类 ID，可为 null
     * @param campusArea 校区/区域，可为 null
     * @param keyword    名称/描述关键词，可为 null
     * @param offset     起始偏移量
     * @param size       每页条数
     * @return 上架美食列表；无数据返回空列表
     */
    List<FoodItemVO> selectOnShelf(@Param("categoryId") Integer categoryId,
                                  @Param("campusArea") String campusArea,
                                  @Param("keyword") String keyword,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    /**
     * 按主键查询上架美食详情。
     *
     * @param id 美食 ID
     * @return 上架美食详情；不存在或未上架返回 null
     */
    FoodItemVO selectOnShelfById(@Param("id") Long id);

    /**
     * 查询上架候选（含口味/食材），供规则推荐打分。
     *
     * @param limit 最多返回条数
     * @return 候选美食列表；无数据返回空列表
     */
    List<FoodItemVO> selectOnShelfForRecommend(@Param("limit") int limit);

    /**
     * 发表评价后累加评价数并重算均分。
     *
     * @param id    美食 ID
     * @param score 新增评价的评分
     * @return 影响行数
     */
    int updateScoreAfterReview(@Param("id") Long id, @Param("score") int score);

    /**
     * 按主键更新美食（动态 SQL，仅更新非 null 字段）。
     *
     * @param entity 含主键的待更新美食
     * @return 影响行数
     */
    int updateById(FoodItem entity);

    /**
     * 逻辑删除（更新 deleted 标记）。
     *
     * @param id 美食 ID
     * @return 影响行数
     */
    int logicalDeleteById(@Param("id") Long id);
}

package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.MealActivity;

/**
 * {@code meal_activity} Mapper。列表筛选、人数增减都在 XML。
 *
 * @author yang
 * @since 2026-11-02
 */
@Mapper
public interface MealActivityMapper {

    /**
     * 插入活动。{@code created_at}/{@code updated_at} 走表默认值，回填 {@code id}。
     *
     * @param row 须带发起人、标题、时间地点、人数上限
     * @return 影响行数
     */
    int insert(MealActivity row);

    /**
     * 按主键查一条。
     *
     * @param id 活动 ID
     * @return 活动；没有返回 null
     */
    MealActivity selectById(@Param("id") Long id);

    /**
     * 按条件统计条数。
     *
     * @param status     活动状态，可为 null
     * @param foodType   美食类型，可为 null
     * @param creatorId  只看某人发起的，可为 null
     * @param joinedUserId 只看某人已报名的，可为 null
     * @return 命中条数
     */
    int countByFilter(@Param("status") Integer status,
                      @Param("foodType") String foodType,
                      @Param("creatorId") Long creatorId,
                      @Param("joinedUserId") Long joinedUserId);

    /**
     * 按条件分页，按发布时间倒序。
     *
     * @param status       活动状态，可为 null
     * @param foodType     美食类型，可为 null
     * @param creatorId    只看某人发起的，可为 null
     * @param joinedUserId 只看某人已报名的，可为 null
     * @param offset       偏移
     * @param size         条数
     * @return 当前页；没有返回空列表
     */
    List<MealActivity> selectByFilter(@Param("status") Integer status,
                                      @Param("foodType") String foodType,
                                      @Param("creatorId") Long creatorId,
                                      @Param("joinedUserId") Long joinedUserId,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    /**
     * 改活动状态。懒更新到点、取消活动用。
     *
     * @param id     活动 ID
     * @param status 目标状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 报名占一个名额：人数 +1，满员则把状态写成已满。
     *
     * <p>WHERE 限定 {@code status=0} 且未满，并发时报名只有一行能成功。
     *
     * @param id 活动 ID
     * @return 影响行数；0 表示已满或状态已变
     */
    int tryIncrementMembers(@Param("id") Long id);

    /**
     * 退出或移出：人数 -1；若原是已满且现在未满，状态回到报名中。
     *
     * @param id 活动 ID
     * @return 影响行数
     */
    int decrementMembers(@Param("id") Long id);
}

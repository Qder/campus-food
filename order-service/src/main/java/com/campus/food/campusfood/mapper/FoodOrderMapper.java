package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.FoodOrder;

/**
 * {@code food_order} Mapper。
 *
 * @author yang
 * @since 2026-11-05
 */
@Mapper
public interface FoodOrderMapper {

    /**
     * 插入订单，回填 {@code id}。
     *
     * @param row 订单主表字段
     * @return 影响行数
     */
    int insert(FoodOrder row);

    /**
     * 按主键查询。
     *
     * @param id 订单 ID
     * @return 订单；不存在返回 null
     */
    FoodOrder selectById(@Param("id") Long id);

    /**
     * 更新订单状态及时间字段（非 null 才更新）。
     *
     * @param row 须带 {@code id}、{@code status}
     * @return 影响行数
     */
    int updateStatus(FoodOrder row);

    /**
     * 统计用户订单数。
     *
     * @param userId 用户 ID
     * @param status 状态，可为 null 表示全部
     * @return 条数
     */
    int countByUserId(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 分页查用户订单。
     *
     * @param userId 用户 ID
     * @param status 状态，可为 null
     * @param offset 偏移
     * @param size   每页条数
     * @return 订单列表
     */
    List<FoodOrder> selectByUserId(@Param("userId") Long userId,
                                   @Param("status") String status,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    /**
     * 统计商家订单数。
     *
     * @param merchantId 店铺 ID
     * @param status     状态，可为 null
     * @return 条数
     */
    int countByMerchantId(@Param("merchantId") Long merchantId, @Param("status") String status);

    /**
     * 分页查商家订单。
     *
     * @param merchantId 店铺 ID
     * @param status     状态，可为 null
     * @param offset     偏移
     * @param size       每页条数
     * @return 订单列表
     */
    List<FoodOrder> selectByMerchantId(@Param("merchantId") Long merchantId,
                                       @Param("status") String status,
                                       @Param("offset") int offset,
                                       @Param("size") int size);
}

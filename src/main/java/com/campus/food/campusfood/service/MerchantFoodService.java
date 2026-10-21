package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;

/**
 * 店主维护本店菜品。
 *
 * @author yang
 * @since 2026-10-16
 */
public interface MerchantFoodService {

    /**
     * 新增菜品并直接上架，绑定当前用户第一家审核通过的店铺。
     *
     * @param dto 菜品信息，其中的 id 不作为主键
     * @return 新菜品 ID
     * @throws com.campus.food.campusfood.exception.BizException 未登录、没有店铺或店铺未审核通过时
     */
    Long save(MerchantFoodSaveDTO dto);

    /**
     * 逻辑删除当前店主名下的菜品。
     *
     * @param id 美食 ID，不能为空
     * @throws com.campus.food.campusfood.exception.BizException 未登录、美食不存在或无权操作时
     */
    void deleteById(Long id);

    /**
     * 更新当前店主名下的菜品。
     *
     * @param id  美食 ID，不能为空
     * @param dto 待写入的菜品字段
     * @return 影响行数
     * @throws com.campus.food.campusfood.exception.BizException 未登录、美食不存在或无权操作时
     */
    int updateById(Long id, MerchantFoodSaveDTO dto) throws RuntimeException;
}

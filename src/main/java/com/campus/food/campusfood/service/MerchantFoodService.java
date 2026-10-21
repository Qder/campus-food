package com.campus.food.campusfood.service;

import java.util.List;

import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodReviewVO;

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

    /**
     * 查看当前店主名下的菜品。
     *
     * @return 当前用户名下各店铺的菜品 VO 列表
     * @throws com.campus.food.campusfood.exception.BizException 未登录时
     */
    List<FoodItemVO> listMine();

    /**
     * 将菜品设为上架（{@code status=1}）。用户端列表/详情仅展示上架菜。
     *
     * @param id 美食 ID，不能为空
     * @throws com.campus.food.campusfood.exception.BizException 未登录、美食不存在、已逻辑删除或无权操作时
     */
    void onShelf(Long id);

    /**
     * 将菜品设为下架（{@code status=2}）。记录保留，用户端不可见，店主仍可在「我的菜品」中查看。
     *
     * @param id 美食 ID，不能为空
     * @throws com.campus.food.campusfood.exception.BizException 未登录、美食不存在、已逻辑删除或无权操作时
     */
    void offShelf(Long id);

    List<FoodReviewVO> listReviews(Long foodId);
}

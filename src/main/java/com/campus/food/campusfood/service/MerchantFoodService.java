package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodReviewVO;
import com.campus.food.campusfood.vo.PageVO;

/**
 * 店主维护本店菜品及查看本人店铺菜品下的用户评价。
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
     * 分页查询当前店主名下的菜品，可按店铺、状态、分类筛选。
     *
     * @param page       页码，从 1 开始，可为 null
     * @param size       每页条数，可为 null
     * @param status     菜品状态，可为 null
     * @param categoryId 分类 ID，可为 null
     * @param merchantId 店铺 ID，可为 null；传入时须属于当前用户
     * @return 分页结果
     * @throws com.campus.food.campusfood.exception.BizException 未登录或无权查看指定店铺时
     */
    PageVO<FoodItemVO> listMine(Integer page, Integer size, Integer status, Integer categoryId, Long merchantId);

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

    /**
     * 分页查看指定菜品下的可见评价。与用户端展示规则一致，但须先校验菜品属于当前店主。
     *
     * @param foodId 美食 ID，不能为空
     * @param page   页码，从 1 开始，可为 null
     * @param size   每页条数，可为 null
     * @return 审核通过且展示中的评价分页结果
     * @throws com.campus.food.campusfood.exception.BizException 未登录、美食不存在或无权查看时
     */
    PageVO<FoodReviewVO> listReviews(Long foodId, Integer page, Integer size);

    //查看单条美食详情
    FoodItemVO getFoodDetail(Long id);
}

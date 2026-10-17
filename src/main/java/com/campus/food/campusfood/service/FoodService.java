package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.vo.FoodCategoryVO;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodRecommendVO;
import com.campus.food.campusfood.vo.PageVO;

import java.util.List;

/**
 * 美食浏览与推荐服务。
 *
 * @author yang
 * @since 2026-10-16
 */
public interface FoodService {

    /**
     * 查询启用中的分类树。
     *
     * @return 分类树根节点列表
     */
    List<FoodCategoryVO> listCategories();

    /**
     * 上架美食分页，可按分类、区域、关键词筛选。
     *
     * @param categoryId 分类 ID，可为 null
     * @param campusArea 校区/区域，可为 null
     * @param keyword    关键词，可为 null；传入空白串会抛异常
     * @param page       页码，可为 null，默认 1
     * @param size       每页条数，可为 null，默认 10，最大 50
     * @return 上架美食分页结果
     * @throws com.campus.food.campusfood.exception.BizException 关键词传入空白串时
     */
    PageVO<FoodItemVO> listFoods(Integer categoryId, String campusArea, String keyword,
                                Integer page, Integer size);

    /**
     * 上架美食详情。
     *
     * @param id 美食 ID
     * @return 美食详情
     * @throws com.campus.food.campusfood.exception.BizException 美食不存在或未上架时
     */
    FoodItemVO getById(Long id);

    /**
     * 规则推荐：须登录且已设置口味；加权排序并过滤忌口。
     *
     * @param userId 当前用户，不可为 null
     * @param limit  返回条数，默认 10，最大 50
     * @return 按推荐分倒序的美食列表
     * @throws com.campus.food.campusfood.exception.BizException 未登录、用户不存在或未设置口味时
     */
    List<FoodRecommendVO> recommend(Long userId, Integer limit);

    /**
     * 根据条件查询商家列表。
     */
    List<Merchant> selectByCondition(MerchantQueryDTO merchantQueryDTO);
}

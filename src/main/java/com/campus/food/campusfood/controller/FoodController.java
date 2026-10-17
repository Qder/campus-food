package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.CreateReviewDTO;
import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.FoodReviewService;
import com.campus.food.campusfood.service.FoodService;
import com.campus.food.campusfood.vo.FoodCategoryVO;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodRecommendVO;
import com.campus.food.campusfood.vo.FoodReviewVO;
import com.campus.food.campusfood.vo.PageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 美食浏览与评价接口。查询类接口大多公开，{@code /food/recommend} 与发表评价需 JWT。
 *
 * @author yang
 * @since 2026-10-16
 */
@RestController
@RequestMapping("/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @Autowired
    private FoodReviewService foodReviewService;

    /**
     * 查看美食分类，无需登录。
     *
     * @return 启用中的分类树
     */
    @GetMapping("/categories")
    public List<FoodCategoryVO> categories() {
        return foodService.listCategories();
    }

    /**
     * 按分类或区域翻页查看美食，无需登录。
     *
     * @param categoryId 分类 ID，可选
     * @param campusArea 校区/区域，可选
     * @param page       页码，可选，默认 1
     * @param size       每页条数，可选，默认 10，最大 50
     * @return 上架美食分页结果
     */
    @GetMapping("/list")
    public PageVO<FoodItemVO> list(@RequestParam(required = false) Integer categoryId,
                                   @RequestParam(required = false) String campusArea,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer size) {
        return foodService.listFoods(categoryId, campusArea, null, page, size);
    }

    /**
     * 关键词搜索美食，无需登录。
     *
     * @param keyword 搜索关键词，不能为空白
     * @param page    页码，可选，默认 1
     * @param size    每页条数，可选，默认 10，最大 50
     * @return 命中的上架美食分页结果
     * @throws BizException 关键词为空白时
     */
    @GetMapping("/search")
    public PageVO<FoodItemVO> search(@RequestParam String keyword,
                                     @RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer size) {
        return foodService.listFoods(null, null, keyword, page, size);
    }

    /**
     * 规则推荐：须登录且已设置口味偏好。
     *
     * @param limit 返回条数，可选，默认 10，最大 50
     * @return 按推荐分倒序的美食列表
     * @throws BizException 未登录、用户不存在或未设置口味时
     */
    @GetMapping("/recommend")
    public List<FoodRecommendVO> recommend(@RequestParam(required = false) Integer limit) {
        return foodService.recommend(UserContext.getUserId(), limit);
    }

    /**
     * 评价列表，无需登录。
     *
     * @param id   美食 ID
     * @param page 页码，可选，默认 1
     * @param size 每页条数，可选，默认 10，最大 50
     * @return 审核通过且展示中的评价分页结果
     * @throws BizException 美食不存在或未上架时
     */
    @GetMapping("/{id}/reviews")
    public PageVO<FoodReviewVO> reviews(@PathVariable Long id,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size) {
        return foodReviewService.listByFoodId(id, page, size);
    }

    /**
     * 发表评价，需 JWT。
     *
     * @param id  美食 ID
     * @param dto 评价内容
     * @return 新建的评价
     * @throws BizException 未登录或美食不可评价时
     */
    @PostMapping("/{id}/reviews")
    public FoodReviewVO createReview(@PathVariable Long id,
                                     @Valid @RequestBody CreateReviewDTO dto) {
        return foodReviewService.create(id, dto);
    }

    /**
     * 查看上架美食详情，无需登录。
     *
     * @param id 美食 ID
     * @return 美食详情
     * @throws BizException 美食不存在或未上架时
     */
    @GetMapping("/{id}")
    public FoodItemVO detail(@PathVariable Long id) {
        return foodService.getById(id);
    }
}

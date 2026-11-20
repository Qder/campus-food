package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.CreateReplyDTO;
import com.campus.food.campusfood.dto.CreateReviewDTO;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.FoodCampaignService;
import com.campus.food.campusfood.service.FoodReviewService;
import com.campus.food.campusfood.service.FoodService;
import com.campus.food.campusfood.vo.*;
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

    @Autowired
    private FoodCampaignService foodCampaignService;

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
     * 查看进行中的店铺营销活动，无需登录。只返回处于起止时间窗内的活动。
     *
     * @param merchantId 店铺 ID，可选，不传表示全部店铺
     * @return 活动列表，带店铺名；无数据返回空数组
     */
    @GetMapping("/campaigns")
    public List<CampaignVO> campaigns(@RequestParam(required = false) Long merchantId) {
        return foodCampaignService.listActive(merchantId);
    }

    /**
     * 协同过滤 + MLP 推荐，须登录。无行为时回退规则或热门。
     *
     * @param limit 返回条数，可选，默认 10，最大 50
     * @return 按推荐分倒序的美食列表
     * @throws BizException 未登录时
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
     * 点赞或取消点赞评价，需 JWT；已点赞再次调用即为取消。
     *
     * @param id 评价 ID
     * @return 切换后的点赞状态与最新点赞数
     * @throws BizException 未登录，或评价不存在/不可见时
     */
    @PostMapping("/reviews/{id}/like")
    public ReviewLikeVO toggleLike(@PathVariable Long id) {
        return foodReviewService.toggleLike(id);
    }

    /**
     * 回复评价，需 JWT。
     *
     * @param id  评价 ID
     * @param dto 回复内容
     * @return 新建的回复
     * @throws BizException 未登录、评价不存在/不可见或内容为空时
     */
    @PostMapping("/reviews/{id}/replies")
    public ReviewReplyVO addReply(@PathVariable Long id,
                                  @Valid @RequestBody CreateReplyDTO dto) {
        return foodReviewService.addReply(id, dto);
    }

    /**
     * 评价回复列表，无需登录，按回复时间正序。
     *
     * @param id 评价 ID
     * @return 回复列表；无回复返回空数组
     * @throws BizException 评价不存在或不可见时
     */
    @GetMapping("/reviews/{id}/replies")
    public List<ReviewReplyVO> replies(@PathVariable Long id) {
        return foodReviewService.listReplies(id);
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

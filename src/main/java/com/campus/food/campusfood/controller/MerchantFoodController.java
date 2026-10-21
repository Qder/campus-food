package com.campus.food.campusfood.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.MerchantFoodService;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodReviewVO;

/**
 * 店主维护本店菜品：新增、修改、上下架、逻辑删除、查询本店列表。均需 JWT；新增须店铺已审核通过。
 *
 * @author yang
 * @since 2026-10-16
 */
@RestController
@RequestMapping("/merchant/foods")
public class MerchantFoodController {
    
    @Autowired
    private MerchantFoodService merchantFoodService;

    /**
     * 新增菜品并直接上架，挂到当前用户第一家审核通过的店。
     *
     * @param dto 菜品信息，其中的 id 不作为主键
     * @return 新菜品 ID
     * @throws BizException 未登录、没有店铺或店铺未审核通过时
     */
    @PostMapping("/save")
    public Long save(@RequestBody MerchantFoodSaveDTO dto) {
        return merchantFoodService.save(dto);
    }

    /**
     * 逻辑删除当前店主名下的菜品。
     *
     * @param id 美食 ID，不能为空
     * @throws BizException 未登录、美食不存在或无权操作时
     */
    @PutMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        merchantFoodService.deleteById(id);
    }

    /**
     * 上架菜品，将 {@code status} 置为 1。与逻辑删除不同，下架菜可再次调用本接口恢复展示。
     *
     * @param id 美食 ID，不能为空
     * @throws BizException 未登录、美食不存在或无权操作时
     */
    @PutMapping("/onshelf/{id}")
    public void onShelf(@PathVariable Long id) {
        merchantFoodService.onShelf(id);
    }

    /**
     * 下架菜品，将 {@code status} 置为 2。用户端不可见，记录仍保留。
     *
     * @param id 美食 ID，不能为空
     * @throws BizException 未登录、美食不存在或无权操作时
     */
    @PutMapping("/offshelf/{id}")
    public void offShelf(@PathVariable Long id) {
        merchantFoodService.offShelf(id);
    }

    /**
     * 更新当前店主名下的菜品。
     *
     * @param id  美食 ID，不能为空
     * @param dto 待写入的菜品字段
     * @throws BizException 未登录、美食不存在或无权操作时
     */
    @PutMapping("/update/{id}")
    public void update(@PathVariable Long id, @RequestBody MerchantFoodSaveDTO dto) {
        merchantFoodService.updateById(id, dto);
    }

    /**
     * 查询当前用户名下各店铺的菜品（含上架、下架、草稿，不含已逻辑删除）。
     *
     * @return 菜品 VO 列表，无数据时为空列表
     * @throws BizException 未登录时
     */
    @GetMapping("/mine")
    public List<FoodItemVO> mine() {
        return merchantFoodService.listMine();
    }

    //商家查看本店菜品的评价
    @GetMapping("/{foodId}/reviews")
    public List<FoodReviewVO> reviews(@PathVariable Long foodId) {
        return merchantFoodService.listReviews(foodId);
    }
}

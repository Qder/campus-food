package com.campus.food.campusfood.controller;

import javax.naming.spi.DirStateFactory.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.MerchantFoodService;

/**
 * 店主维护本店菜品：新增、修改、逻辑删除。均需 JWT，且店铺须已审核通过。
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
}

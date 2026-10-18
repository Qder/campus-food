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
import com.campus.food.campusfood.service.MerchantFoodService;

@RestController
@RequestMapping("/merchant/foods")
public class MerchantFoodController {
    
    @Autowired
    private MerchantFoodService merchantFoodService;

    @PostMapping("/save")
    public Long save(@RequestBody MerchantFoodSaveDTO dto) {
        return merchantFoodService.save(dto);
    }

    @PutMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        merchantFoodService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public void update(@PathVariable Long id, @RequestBody MerchantFoodSaveDTO dto) {
        merchantFoodService.updateById(id, dto);
    }
}

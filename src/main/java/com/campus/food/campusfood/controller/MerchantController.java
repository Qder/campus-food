package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;


    @PostMapping("/insert")
    public int insert(@RequestBody Merchant merchant) {
        return merchantService.insert(merchant);
    }
    @PutMapping("/update")
    public int update(@RequestBody Merchant merchant) {
        return merchantService.update(merchant);
    }

    @PutMapping("/delete/{id}")
    public int delete(@PathVariable Long id) {
        return merchantService.deleteById(id);
    }

    @GetMapping("/list")
    public List<Merchant> list() {
        return merchantService.selectAll();
    }

    @GetMapping("/get/{id}")
    public Merchant get(@PathVariable Long id) {
        return merchantService.selectById(id);
    }

    @GetMapping("/mine")
    public List<Merchant> mine() {
        return merchantService.listMine();
    }
    @PostMapping("/listByCondition")
    public List<Merchant> listByCondition(@RequestBody MerchantQueryDTO merchantQueryDTO) {
        return merchantService.selectByCondition(merchantQueryDTO);
    }
}

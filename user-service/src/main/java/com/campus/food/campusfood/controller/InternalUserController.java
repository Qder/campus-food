package com.campus.food.campusfood.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.service.UserService;

@RestController
@RequestMapping("/internal")
public class InternalUserController {

    @Autowired
    private UserService userService;

    
    @GetMapping("/users/{id}")
    public UserInfo getUserInfo(@PathVariable("id") Long id) {
        return userService.getUserInfo(id);
    }
    
    @PutMapping("/users/{id}/merchant-role")
    public void setMerchantRole(@PathVariable("id") Long id) {
        userService.setMerchantRole(id);
    }
}

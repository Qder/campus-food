package com.campus.food.campusfood.controller;

import javax.naming.spi.DirStateFactory.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.entity.FriendRequest;
import com.campus.food.campusfood.service.SocialService;
import com.campus.food.campusfood.vo.FriendRequestVo;

@RestController
@RequestMapping("/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    @PostMapping("/sendFriendRequest")
    public FriendRequestVo sendFriendRequest(@RequestBody FriendRequest friendRequest) {
        return socialService.sendFriendRequest(friendRequest);
    }

    @PostMapping("/handleFriendRequest")
    public FriendRequestVo handleFriendRequest(@RequestBody FriendRequestVo friendRequestVo) {
        return socialService.handleFriendRequest(friendRequestVo);
    }
    
    
}

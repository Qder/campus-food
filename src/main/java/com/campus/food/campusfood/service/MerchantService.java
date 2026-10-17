package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.entity.Merchant;

import java.util.List;

/**
 * 商家店铺业务。
 */
public interface MerchantService {

    /**
     * 入驻申请，绑定当前登录用户为店主。
     */
    int insert(Merchant merchant);

    /**
     * 店主逻辑删除自己的店铺。
     */
    int deleteById(Long id);

    /**
     * 按店主用户 ID 查询其名下的店铺（无数据返回空列表）。
     */
    List<Merchant> selectByOwnerUserId(Long ownerUserId);

    /**
     * 按条件查询店铺（条件由 DTO 决定，无数据返回空列表）。
     */
    List<Merchant> selectByCondition(MerchantQueryDTO merchantQueryDTO);

    /**
     * 按主键查询未删除店铺；不存在返回 null。
     */
    Merchant selectById(Long id);

    /**
     * 查询全部未删除店铺。
     */
    List<Merchant> selectAll();

    /**
     * 店主更新自己的店铺（动态字段）。
     */
    int update(Merchant merchant);

    /**
     * 用户端店铺详情：须审核通过且营业中。
     */
    Merchant getVisibleById(Long id);

    /**
     * 当前登录店主名下的店铺列表。
     */
    List<Merchant> listMine();
}

package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.entity.Merchant;

import java.util.List;

/**
 * 商家店铺业务：入驻、维护，以及用户端可见店铺查询。
 *
 * @author yang
 * @since 2026-10-16
 */
public interface MerchantService {

    /**
     * 提交入驻申请。店主固定为当前登录用户，未指定时默认待审核、未开业。
     *
     * @param merchant 店铺资料，不能为空；请求里的店主 ID 会被当前用户覆盖
     * @return 影响行数
     * @throws com.campus.food.campusfood.exception.BizException 未登录、店铺信息为空或写入失败时
     */
    int insert(Merchant merchant);

    /**
     * 店主逻辑删除自己的店铺。
     *
     * @param id 店铺 ID，不能为空
     * @return 影响行数
     * @throws com.campus.food.campusfood.exception.BizException 未登录、店铺不存在、无权操作或删除失败时
     */
    int deleteById(Long id);

    /**
     * 按店主用户 ID 查询其名下未删除的店铺。
     *
     * @param ownerUserId 店主用户 ID，为 null 时返回空列表
     * @return 店铺列表，无数据时为空列表
     */
    List<Merchant> selectByOwnerUserId(Long ownerUserId);

    /**
     * 按名称、地址、审核状态、经营状态查询未删除店铺。
     *
     * @param merchantQueryDTO 查询条件，可为 null，字段均可空
     * @return 命中的店铺列表，无数据时为空列表
     */
    List<Merchant> selectByCondition(MerchantQueryDTO merchantQueryDTO);

    /**
     * 按主键查询未删除店铺。
     *
     * @param id 店铺 ID，为 null 时返回 null
     * @return 店铺；不存在时为 null
     */
    Merchant selectById(Long id);

    /**
     * 查询审核通过且营业中的未删除店铺。
     *
     * @return 店铺列表，无数据时为空列表
     */
    List<Merchant> selectAll();

    /**
     * 店主更新自己的店铺，只写非空字段。
     *
     * @param merchant 含主键的店铺资料，不能为空
     * @return 影响行数
     * @throws com.campus.food.campusfood.exception.BizException 未登录、主键为空、店铺不存在、无权操作或更新失败时
     */
    int update(Merchant merchant);

    /**
     * 用户端店铺详情。须审核通过且营业中，避免未开业店铺被浏览。
     *
     * @param id 店铺 ID，不能为空
     * @return 可见店铺
     * @throws com.campus.food.campusfood.exception.BizException 店铺不存在、未审核通过或未营业时
     */
    Merchant getVisibleById(Long id);

    /**
     * 当前登录用户名下的店铺。
     *
     * @return 店铺列表，无数据时为空列表
     * @throws com.campus.food.campusfood.exception.BizException 未登录时
     */
    List<Merchant> listMine();
}

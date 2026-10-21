package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.dto.MerchantQueryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * {@code merchant} 表的 MyBatis Mapper。查询默认排除已逻辑删除的行。
 *
 * @author yang
 * @since 2026-10-16
 */
@Mapper
public interface MerchantMapper {

    /**
     * 新增店铺。
     *
     * @param merchant 待插入的店铺
     * @return 影响行数
     */
    int insert(Merchant merchant);

    /**
     * 按主键查询未删除店铺。
     *
     * @param id 店铺 ID
     * @return 店铺；不存在返回 null
     */
    Merchant selectById(Long id);

    /**
     * 查询审核通过且营业中的未删除店铺。待审或停业的店不出现在这份列表里。
     *
     * @return 店铺列表；无数据返回空列表
     */
    List<Merchant> selectAll();

    /**
     * 按主键更新店铺（动态 SQL，仅更新非 null 字段）。
     *
     * @param merchant 含主键的待更新店铺
     * @return 影响行数
     */
    int update(Merchant merchant);

    /**
     * 逻辑删除店铺。
     *
     * @param id 店铺 ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 按名称、地址、审核状态、经营状态查询未删除店铺。
     *
     * @param merchantQueryDTO 查询条件
     * @return 命中的店铺列表；无数据返回空列表
     */
    List<Merchant> selectByCondition(MerchantQueryDTO merchantQueryDTO);

    /**
     * 按店主用户 ID 查询未删除店铺。
     *
     * @param ownerUserId 店主用户 ID
     * @return 店铺列表；无数据返回空列表
     */
    List<Merchant> selectByOwnerUserId(Long ownerUserId);
}

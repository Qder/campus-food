package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.dto.MerchantQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MerchantMapper {

    //新增商店
    int insert(Merchant merchant);

    //按主键查询商店
    Merchant selectById(Long id);

    //查询所有商店
    List<Merchant> selectAll();

    //更新商店
    int update(Merchant merchant);

    //逻辑删除商店
    int deleteById(Long id);

    //按条件查询商店
    List<Merchant> selectByCondition(MerchantQueryDTO merchantQueryDTO);

    //按ownerUserId查询商店
    List<Merchant> selectByOwnerUserId(Long ownerUserId);
}

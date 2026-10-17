package com.campus.food.campusfood.mapper;


import com.campus.food.campusfood.dto.MerchantRejectDTO;
import com.campus.food.campusfood.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMapper {

    List<Merchant> getPendingMerchants();

    Merchant createMerchant(Merchant merchant);

    int updateMerchantStatus(Long id, Integer status);
}

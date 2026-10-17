package com.campus.food.campusfood.service;

import java.util.List;

import com.campus.food.campusfood.dto.MerchantRejectDTO;
import com.campus.food.campusfood.entity.Merchant;


public interface AdminService {

    List<Merchant> getPendingMerchants();

    Merchant createMerchant(Merchant merchant);

    Merchant updateMerchant(Long id, Merchant merchant);

    Merchant rejectMerchant(Long id, MerchantRejectDTO merchantRejectDTO);
}

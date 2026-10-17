package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.aspect.AdminOnly;
import com.campus.food.campusfood.dto.MerchantRejectDTO;
import com.campus.food.campusfood.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.campus.food.campusfood.entity.Merchant;

@RestController
@RequestMapping("/admin")
@AdminOnly
public class AdminController {

    @Autowired
    private AdminService adminService;


    @GetMapping("/merchants/pending")
    public ResponseEntity<List<Merchant>> getPendingMerchants() {
        return ResponseEntity.ok(adminService.getPendingMerchants());
    }

    @PostMapping("/merchants")
    public ResponseEntity<Merchant> createMerchant(@RequestBody Merchant merchant) {
        return ResponseEntity.ok(adminService.createMerchant(merchant));
    }

    @PutMapping("/merchants/{id}/approve")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable Long id, @RequestBody Merchant merchant) {
        return ResponseEntity.ok(adminService.updateMerchant(id, merchant));
    }

    @PutMapping("/merchants/{id}/reject")
    public ResponseEntity<Merchant> rejectMerchant(@PathVariable Long id, @RequestBody MerchantRejectDTO merchantRejectDTO) {
        return ResponseEntity.ok(adminService.rejectMerchant(id, merchantRejectDTO));
    }
}

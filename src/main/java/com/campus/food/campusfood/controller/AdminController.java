package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.aspect.AdminOnly;
import com.campus.food.campusfood.dto.MerchantCreateDTO;
import com.campus.food.campusfood.dto.MerchantRejectDTO;
import com.campus.food.campusfood.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.entity.Merchant;

import java.util.List;

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
    public ResponseEntity<Merchant> createMerchant(@Valid @RequestBody MerchantCreateDTO merchantCreateDTO) {
        return ResponseEntity.ok(adminService.createMerchant(merchantCreateDTO));
    }

    @PutMapping("/merchants/{id}/approve")
    public ResponseEntity<Merchant> approveMerchant(@PathVariable Long id,
                                                    @RequestParam(required = false) String auditRemark) {
        return ResponseEntity.ok(adminService.approveMerchant(id, auditRemark));
    }

    @PutMapping("/merchants/{id}/reject")
    public ResponseEntity<Merchant> rejectMerchant(@PathVariable Long id,
                                                   @Valid @RequestBody MerchantRejectDTO merchantRejectDTO) {
        return ResponseEntity.ok(adminService.rejectMerchant(id, merchantRejectDTO));
    }
}

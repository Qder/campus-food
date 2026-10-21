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
import com.campus.food.campusfood.exception.BizException;

import java.util.List;

/**
 * 平台管理员的店铺审核接口。类上的 {@link AdminOnly} 要求角色为管理员，且请求须带 JWT。
 *
 * @author yang
 * @since 2026-10-16
 */
@RestController
@RequestMapping("/admin")
@AdminOnly
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 查询待审核店铺。
     *
     * @return 待审店铺列表，无数据时为空列表
     */
    @GetMapping("/merchants/pending")
    public ResponseEntity<List<Merchant>> getPendingMerchants() {
        return ResponseEntity.ok(adminService.getPendingMerchants());
    }

    /**
     * 代建店铺。创建后直接记为审核通过并开业，同时把店主角色升为商家。
     *
     * @param merchantCreateDTO 店铺与店主信息
     * @return 落库后的店铺
     * @throws BizException 店主不存在或写入失败时
     */
    @PostMapping("/merchants")
    public ResponseEntity<Merchant> createMerchant(@Valid @RequestBody MerchantCreateDTO merchantCreateDTO) {
        return ResponseEntity.ok(adminService.createMerchant(merchantCreateDTO));
    }

    /**
     * 通过待审店铺。通过后店铺开业，并授予店主商家角色。
     *
     * @param id          店铺 ID
     * @param auditRemark 审核备注，可选
     * @return 更新后的店铺
     * @throws BizException 未登录、店铺不存在或不是待审时
     */
    @PutMapping("/merchants/{id}/approve")
    public ResponseEntity<Merchant> approveMerchant(@PathVariable Long id,
                                                    @RequestParam(required = false) String auditRemark) {
        return ResponseEntity.ok(adminService.approveMerchant(id, auditRemark));
    }

    /**
     * 驳回待审店铺，必须填写原因。
     *
     * @param id                店铺 ID
     * @param merchantRejectDTO 驳回原因
     * @return 更新后的店铺
     * @throws BizException 店铺不是待审或原因为空时
     */
    @PutMapping("/merchants/{id}/reject")
    public ResponseEntity<Merchant> rejectMerchant(@PathVariable Long id,
                                                   @Valid @RequestBody MerchantRejectDTO merchantRejectDTO) {
        return ResponseEntity.ok(adminService.rejectMerchant(id, merchantRejectDTO));
    }
}

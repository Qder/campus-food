package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺入驻与维护接口。均需 JWT；修改和删除只能作用于当前用户自己的店。
 *
 * @author yang
 * @since 2026-10-16
 */
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 提交入驻申请。店主固定为当前登录用户，默认待审核、未开业。
     *
     * @param merchant 店铺资料；请求里的店主 ID 会被当前用户覆盖
     * @return 影响行数
     * @throws BizException 未登录或店铺信息为空时
     */
    @PostMapping("/insert")
    public int insert(@RequestBody Merchant merchant) {
        return merchantService.insert(merchant);
    }

    /**
     * 店主更新自己的店铺，只写请求里带上的字段。
     *
     * @param merchant 含主键的店铺资料
     * @return 影响行数
     * @throws BizException 未登录、店铺不存在或无权操作时
     */
    @PutMapping("/update")
    public int update(@RequestBody Merchant merchant) {
        return merchantService.update(merchant);
    }

    /**
     * 店主逻辑删除自己的店铺。
     *
     * @param id 店铺 ID，不能为空
     * @return 影响行数
     * @throws BizException 未登录、店铺不存在或无权操作时
     */
    @PutMapping("/delete/{id}")
    public int delete(@PathVariable Long id) {
        return merchantService.deleteById(id);
    }

    /**
     * 查询审核通过且营业中的店铺。
     *
     * @return 店铺列表，无数据时为空列表
     */
    @GetMapping("/list")
    public List<Merchant> list() {
        return merchantService.selectAll();
    }

    /**
     * 按主键查询未删除店铺。
     *
     * @param id 店铺 ID
     * @return 店铺；不存在时为 null
     */
    @GetMapping("/get/{id}")
    public Merchant get(@PathVariable Long id) {
        return merchantService.selectById(id);
    }

    /**
     * 查询当前登录用户名下的店铺。
     *
     * @return 店铺列表，无数据时为空列表
     * @throws BizException 未登录时
     */
    @GetMapping("/mine")
    public List<Merchant> mine() {
        return merchantService.listMine();
    }

    /**
     * 按名称、地址、审核状态、经营状态筛选店铺。
     *
     * @param merchantQueryDTO 查询条件，字段均可空
     * @return 命中的店铺列表，无数据时为空列表
     */
    @PostMapping("/listByCondition")
    public List<Merchant> listByCondition(@RequestBody MerchantQueryDTO merchantQueryDTO) {
        return merchantService.selectByCondition(merchantQueryDTO);
    }
}

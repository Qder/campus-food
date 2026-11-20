package com.campus.food.campusfood.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 营销活动展示对象。商家端列表带效果统计，用户端列表统计字段为 null。
 *
 * @author yang
 * @since 2026-11-20
 */
@Data
public class CampaignVO {

    /**
     * 活动ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动类型:DISCOUNT/FULL_REDUCE/GROUP_BUY
     */
    private String campaignType;

    /**
     * 规则JSON字符串，如 {"full":30,"reduce":5}
     */
    private String ruleJson;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

    /**
     * 状态:0草稿1进行中2结束3下线
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 参与人数合计，仅商家端返回
     */
    private Integer participantCnt;

    /**
     * 订单数合计，仅商家端返回
     */
    private Integer orderCnt;

    /**
     * 销售额合计，仅商家端返回
     */
    private BigDecimal salesAmount;

}
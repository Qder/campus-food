package com.campus.food.campusfood.enums;

/**
 * 由列 {@code marketing_campaign.campaign_type} 的 COMMENT 解析生成。
 * of() 对未知类型统一返回 null。
 *
 * @author yang
 * @since 2026-11-20
 */
public enum CampaignType {

    /** 打折 */
    DISCOUNT,
    /** 满减 */
    FULL_REDUCE,
    /** 团购 */
    GROUP_BUY;

    /**
     * 按类型名反查枚举。
     *
     * @param name 类型名，可为 null
     * @return 匹配的枚举；null 或未知类型返回 null
     */
    public static CampaignType of(String name) {
        if (name == null) {
            return null;
        }
        for (CampaignType e : values()) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        return null;
    }
}
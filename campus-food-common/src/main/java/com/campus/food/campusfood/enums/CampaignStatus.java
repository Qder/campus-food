package com.campus.food.campusfood.enums;

/**
 * 由列 {@code marketing_campaign.status} 的 COMMENT 解析生成。
 * of() 对未知码统一返回 null。
 *
 * @author yang
 * @since 2026-11-20
 */
public enum CampaignStatus {

    /** 草稿 */
    DRAFT(0),
    /** 进行中 */
    ACTIVE(1),
    /** 已结束 */
    ENDED(2),
    /** 已下线 */
    OFFLINE(3);

    /** 落库的状态码 */
    private final int code;

    CampaignStatus(int code) {
        this.code = code;
    }

    /**
     * 取状态码。
     *
     * @return 落库用的状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 按状态码反查枚举。
     *
     * @param code 状态码，可为 null
     * @return 匹配的枚举；null 或未知码返回 null
     */
    public static CampaignStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (CampaignStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
package com.campus.food.campusfood.enums;

/**
 * 店铺审核状态，对应列 {@code merchant.audit_status}。
 * 未知码由 {@link #of(Integer)} 返回 null，避免把脏数据当成合法状态。
 *
 * @author yang
 * @since 2026-10-16
 */
public enum MerchantAuditStatus {

    /** 待审 */
    PENDING(0),
    /** 通过 */
    APPROVED(1),
    /** 驳回 */
    REJECTED(2),
    /** 取消资格 */
    DISQUALIFIED(3);

    /** 落库的审核码 */
    private final int code;

    MerchantAuditStatus(int code) {
        this.code = code;
    }

    /**
     * 取审核码。
     *
     * @return 落库用的审核码
     */
    public int getCode() {
        return code;
    }

    /**
     * 按审核码反查枚举。
     *
     * @param code 审核码，可为 null
     * @return 匹配的枚举；null 或未知码返回 null
     */
    public static MerchantAuditStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (MerchantAuditStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}

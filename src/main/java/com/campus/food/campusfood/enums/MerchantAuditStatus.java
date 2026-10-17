package com.campus.food.campusfood.enums;

/**
 * 由列 {@code merchant.audit_status} 的 COMMENT 解析生成。
 * 禁止手工臆造常量；解析失败则不会生成此文件。
 * of() 对未知码统一返回 null。
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

    private final int code;

    MerchantAuditStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

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

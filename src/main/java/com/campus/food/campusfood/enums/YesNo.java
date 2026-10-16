package com.campus.food.campusfood.enums;

/**
 * 由列 {@code sys_user.privacy_feed} 的 COMMENT 解析生成。
 * 禁止手工臆造常量；解析失败则不会生成此文件。
 * of() 对未知码统一返回 null。
 *
 * @author yang
 * @since 2026-10-16
 */
public enum YesNo {

    /** 否 */
    NO(0),
    /** 是 */
    YES(1);

    /** 落库的布尔码 */
    private final int code;

    YesNo(int code) {
        this.code = code;
    }

    /**
     * 取布尔码。
     *
     * @return 0 表示否，1 表示是
     */
    public int getCode() {
        return code;
    }

    /**
     * 按布尔码反查枚举。
     *
     * @param code 布尔码，可为 null
     * @return 匹配的枚举；null 或未知码返回 null
     */
    public static YesNo of(Integer code) {
        if (code == null) {
            return null;
        }
        for (YesNo e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}

package com.campus.food.campusfood.enums;

/**
 * 由列 {@code sys_user.gender} 的 COMMENT 解析生成。
 * 禁止手工臆造常量；解析失败则不会生成此文件。
 *
 * @author yang
 * @since 2026-10-16
 */
public enum UserGenderEnum {

    /** 未知 */
    UNKNOWN(0),
    /** 男 */
    MALE(1),
    /** 女 */
    FEMALE(2);

    /** 落库的性别码 */
    private final int code;

    UserGenderEnum(int code) {
        this.code = code;
    }

    /**
     * 取性别码。
     *
     * @return 落库用的性别码
     */
    public int getCode() {
        return code;
    }

    /**
     * 按性别码反查枚举。
     *
     * @param code 性别码，可为 null
     * @return 匹配的枚举；null 或未知码返回 null
     */
    public static UserGenderEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserGenderEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}

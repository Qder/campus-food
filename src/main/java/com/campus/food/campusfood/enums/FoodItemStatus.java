package com.campus.food.campusfood.enums;

/**
 * 由列 {@code food_item.status} 的 COMMENT 解析生成。
 * 禁止手工臆造常量；解析失败则不会生成此文件。
 * of() 对未知码统一返回 null。
 *
 * @author yang
 * @since 2026-10-16
 */
public enum FoodItemStatus {

    /** 草稿 */
    DRAFT(0),
    /** 上架 */
    ON_SHELF(1),
    /** 下架 */
    OFF_SHELF(2);

    /** 落库的状态码 */
    private final int code;

    FoodItemStatus(int code) {
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
    public static FoodItemStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (FoodItemStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}

package com.campus.food.campusfood.entity;

import com.campus.food.campusfood.enums.YesNo;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 美食分类，对应表 {@code food_category}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodCategory {

    /**
     * 分类ID
     */
    private Integer id;

    /**
     * 父分类ID,0为顶级
     */
    private Integer parentId;

    /**
     * 分类名(中餐/西餐/小吃等)
     */
    private String name;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 1启用0停用
     * {@link YesNo} 0=否, 1=是
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}

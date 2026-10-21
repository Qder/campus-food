package com.campus.food.campusfood.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 美食分类视图对象。通过 {@code children} 组成分类树。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class FoodCategoryVO {

    /** 分类 ID */
    private Integer id;

    /** 父分类 ID，0 为顶级 */
    private Integer parentId;

    /** 分类名 */
    private String name;

    /** 同级排序号，越小越靠前 */
    private Integer sortNo;

    /** 子分类，永不为 null */
    private List<FoodCategoryVO> children = new ArrayList<>();
}

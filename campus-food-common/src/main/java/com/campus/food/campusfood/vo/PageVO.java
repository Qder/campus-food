package com.campus.food.campusfood.vo;

import lombok.Data;

import java.util.List;

/**
 * 通用分页结果包装。
 *
 * @param <T> 列表元素类型
 * @author yang
 * @since 2026-10-16
 */
@Data
public class PageVO<T> {

    /** 总记录数 */
    private long total;

    /** 当前页码，从 1 开始 */
    private int page;

    /** 每页条数 */
    private int size;

    /** 当前页数据 */
    private List<T> list;
}

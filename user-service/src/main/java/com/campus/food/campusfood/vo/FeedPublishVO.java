package com.campus.food.campusfood.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发布动态成功体，对应 V1.3 {@code POST /user/feeds}。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedPublishVO {

    /** 动态主键 */
    private Long feedId;

    /** 审核状态；无审核流，固定 0 */
    private Integer auditStatus;
}

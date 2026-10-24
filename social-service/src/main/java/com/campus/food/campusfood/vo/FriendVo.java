package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 好友列表项，对应 {@code GET /social/friends} 数组中的一个元素。
 *
 * <p>只暴露展示所需的字段，不把跨服务传递的 {@link com.campus.food.campusfood.feign.UserInfo}
 * 原样返回，避免口味标签、忌口、删除标记等内部字段外泄。
 *
 * @author yang
 * @since 2026-10-24
 */
@Data
public class FriendVo {

    /** 好友用户 ID */
    private Long id;

    /** 好友昵称 */
    private String nickname;

    /** 好友头像 URL，可为空 */
    private String avatarUrl;
}

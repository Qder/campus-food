package com.campus.food.campusfood.feign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 跨服务传递的用户摘要，供推荐打分、商家升权等内部调用使用（不含密码、手机号等敏感字段）。
 *
 * @author yang
 * @since 2026-10-22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    /** 用户主键 */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 头像 URL，好友列表等展示场景使用 */
    private String avatarUrl;

    /** 口味标签，逗号分隔，与 {@link com.campus.food.campusfood.entity.User#tasteTags} 一致 */
    private String tasteTags;

    /** 忌口食材，逗号分隔 */
    private String dislikeIngredients;

    /** 逻辑删除标记：0 正常，非 0 已删除 */
    private Integer deleted;

    /** 是否允许陌生人加好友：0 否，1 是，对应 {@code sys_user.privacy_add_friend} */
    private Integer privacyAddFriend;

    /** 角色 ID，与库表 role_id 一致（如商家为 2） */
    private Integer roleId;
}

package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求体，对应 {@code POST /auth/register}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class RegisterDTO {

    /** 手机号，与邮箱二选一；空串表示未填 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱，与手机号二选一；空串表示未填 */
    @Pattern(regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "邮箱格式不正确")
    private String email;

    /** 明文密码，入库前在 Service 中做 BCrypt */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6到32个字符之间")
    @Pattern(regexp = "^\\S+$", message = "密码不能包含空格")
    private String password;

    /** 用户昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称长度不能超过 20 位")
    private String nickname;

    /**
     * Bean Validation 回调：手机号、邮箱至少填一项。
     *
     * @return 至少有一项非空白时为 true
     */
    @AssertTrue(message = "手机号或邮箱至少填写一个")
    public boolean isAccountPresent() {
        return hasText(phone) || hasText(email);
    }

    /** 去空白后是否非空，供 {@link #isAccountPresent()} 使用 */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

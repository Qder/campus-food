package com.campus.food.campusfood.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterDTO {

    /** 手机号，可空；与邮箱至少填一个，非空时校验格式 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    /** 邮箱，至少一个 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 明文密码，仅用于 DTO，不入库 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    @Pattern(regexp = "^\\S+$", message = "密码不能包含空格")
    private String password;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称长度不能超过 20 位")
    private String nickname;
}

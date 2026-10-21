package com.campus.food.campusfood.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记仅平台管理员（{@code roleId=3}）可访问的接口，由 {@link AdminOnlyAspect} 拦截。
 *
 * @author yang
 * @since 2026-10-16
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOnly {
}

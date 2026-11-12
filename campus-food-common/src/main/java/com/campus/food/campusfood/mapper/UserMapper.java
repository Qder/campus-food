package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.User;
import com.campus.food.campusfood.vo.UserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@code sys_user} MyBatis Mapper
 *
 * <p>
 * 常用查询键（来自 UNIQUE/INDEX，仅注释提示，不自动生成方法）：
 * - UNIQUE PRIMARY: id → 建议 selectById
 * - UNIQUE uk_user_phone: phone → 建议 selectByPhone
 * - UNIQUE uk_user_email: email → 建议 selectByEmail
 * - INDEX idx_user_school: school → 建议 selectBySchool
 * - INDEX idx_user_status: status → 建议 selectByStatus
 * - INDEX idx_user_created: created_at → 建议 selectByCreatedAt
 * - INDEX idx_user_role: role_id → 建议 selectByRoleId
 *
 * @author yang
 * @since 2026-10-16
 */
@Mapper
public interface UserMapper {

    /**
     * 统计手机号已占用的用户数。
     *
     * @param phone 手机号
     * @return 匹配条数
     */
    int countByPhone(@Param("phone") String phone);

    /**
     * 统计邮箱已占用的用户数。
     *
     * @param email 邮箱
     * @return 匹配条数
     */
    int countByEmail(@Param("email") String email);

    /**
     * 按手机号查询用户。
     *
     * @param phone 手机号
     * @return 用户记录；不存在返回 null
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 按邮箱查询用户。
     *
     * @param email 邮箱
     * @return 用户记录；不存在返回 null
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 新增用户。
     *
     * @param entity 待插入的用户
     * @return 影响行数
     */
    int insert(User entity);

    /**
     * 按主键查询用户。
     *
     * @param id 用户 ID
     * @return 用户记录；不存在返回 null
     */
    User selectById(@Param("id") Long id);

    /**
     * 按主键更新用户（动态 SQL，仅更新非 null 字段）。
     *
     * @param entity 含主键的待更新用户
     * @return 影响行数
     */
    int updateById(User entity);

    /**
     * 更新最近登录时间。
     *
     * @param id          用户 ID
     * @param lastLoginAt 登录时间
     * @return 影响行数
     */
    int updateLastLoginAt(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    /**
     * 逻辑删除（更新 deleted 标记）。
     *
     * @param id 用户 ID
     * @return 影响行数
     */
    int logicalDeleteById(@Param("id") Long id);

    /**
     * 管理端用户列表总条数（未删除）。
     *
     * @param keyword 昵称 / 手机 / 邮箱模糊匹配，可为 null
     * @param status  账号状态，可为 null 表示不限
     * @return 匹配条数
     */
    int countByAdminFilter(@Param("keyword") String keyword, @Param("status") Integer status);

    /**
     * 管理端用户分页列表（未删除）。
     *
     * @param keyword 昵称 / 手机 / 邮箱模糊匹配，可为 null
     * @param status  账号状态，可为 null 表示不限
     * @param offset  跳过条数
     * @param size    取条数
     * @return 当前页，无数据时为空列表
     */
    List<UserListVO> selectPageByAdminFilter(@Param("keyword") String keyword,
                                             @Param("status") Integer status,
                                             @Param("offset") int offset,
                                             @Param("size") int size);
}

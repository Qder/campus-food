package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.FriendRequest;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * {@code friend_request} 表 MyBatis Mapper。
 *
 * <p>常用索引（仅注释提示，不自动生成方法）：
 * - INDEX idx_fr_to_status: to_user_id, status → 收件箱查询
 * - INDEX idx_fr_from: from_user_id → 我发出的申请
 *
 * @author yang
 * @since 2026-10-23
 */
@Mapper
public interface FriendRequestMapper {

    /**
     * 新增好友申请。
     *
     * @param friendRequest 待插入的申请
     * @return 影响行数
     */
    int insert(FriendRequest friendRequest);

    /**
     * 按主键更新申请（动态 SQL，仅更新非 null 字段）。
     *
     * @param friendRequest 含主键的待更新申请
     * @return 影响行数
     */
    int update(FriendRequest friendRequest);

    /**
     * 按主键删除申请。
     *
     * @param id 申请 ID
     * @return 影响行数
     */
    int delete(Long id);

    /**
     * 按主键查询申请。
     *
     * @param id 申请 ID
     * @return 申请记录；不存在返回 null
     */
    FriendRequest selectById(Long id);

    /**
     * 查询与指定用户相关的申请（我发出的 + 我收到的）。
     *
     * @param userId 用户 ID
     * @return 申请列表；无数据返回空列表
     */
    List<FriendRequest> selectByUserId(Long userId);

    /**
     * 查询全部申请。
     *
     * @return 申请列表；无数据返回空列表
     */
    List<FriendRequest> selectAll();

    /**
     * 统计两人之间待处理的申请数，用于防止重复申请。
     *
     * @param fromUserId 申请人用户 ID
     * @param toUserId   被申请人用户 ID
     * @return 待处理申请条数
     */
    int selectPending(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /**
     * 查询某人收到的待处理申请（收件箱）。
     *
     * @param toUserId 被申请人用户 ID
     * @param status   状态过滤，可为 null（当前 XML 固定查询待处理 0）
     * @return 待处理申请列表；无数据返回空列表
     */
    List<FriendRequest> selectInbox(@Param("toUserId") Long toUserId, @Param("status") Integer status);
}

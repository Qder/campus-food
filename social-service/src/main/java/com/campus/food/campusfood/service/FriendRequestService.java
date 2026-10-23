package com.campus.food.campusfood.service;

import com.campus.food.campusfood.entity.FriendRequest;

import java.util.List;

/**
 * 好友申请服务。
 *
 * <p>负责申请的落库与查询；同意后写入 {@link FriendshipService} 的双向关系由调用方编排。
 *
 * @author yang
 * @since 2026-10-23
 */
public interface FriendRequestService {

    /**
     * 新增好友申请。
     *
     * @param friendRequest 待插入的申请
     * @return 影响行数
     */
    int insert(FriendRequest friendRequest);

    /**
     * 更新好友申请（如回填处理状态与处理时间）。
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
     * 查询某人收到的待处理申请。
     *
     * @param touserId 被申请人用户 ID
     * @param status   状态过滤，可为 null
     * @return 申请列表；无数据返回空列表
     */
    List<FriendRequest> selectInbox(Long touserId, Integer status);

    /**
     * 判断两人之间是否已有待处理申请，用于防重复提交。
     *
     * @param fromuserId 申请人用户 ID
     * @param touserId   被申请人用户 ID
     * @return true 表示已存在待处理申请
     */
    boolean selectPending(Long fromuserId, Long touserId);
}

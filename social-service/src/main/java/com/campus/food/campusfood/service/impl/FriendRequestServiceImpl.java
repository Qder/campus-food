package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.entity.FriendRequest;
import com.campus.food.campusfood.mapper.FriendRequestMapper;
import com.campus.food.campusfood.service.FriendRequestService;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友申请服务实现。
 *
 * <p>当前为直接透传 Mapper 的薄封装，申请判重、同意后建关系等编排由上层负责。
 *
 * @author yang
 * @since 2026-10-23
 */
@Service
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestMapper friendRequestMapper;

    /**
     * 构造注入 Mapper。
     *
     * @param friendRequestMapper 好友申请 Mapper
     */
    public FriendRequestServiceImpl(FriendRequestMapper friendRequestMapper) {
        this.friendRequestMapper = friendRequestMapper;
    }

    /** {@inheritDoc} */
    @Override
    public int insert(FriendRequest friendRequest) {
        return friendRequestMapper.insert(friendRequest);
    }

    /** {@inheritDoc} */
    @Override
    public int update(FriendRequest friendRequest) {
        return friendRequestMapper.update(friendRequest);
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Long id) {
        return friendRequestMapper.delete(id);
    }

    /** {@inheritDoc} */
    @Override
    public FriendRequest selectById(Long id) {
        return friendRequestMapper.selectById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<FriendRequest> selectByUserId(Long userId) {
        return friendRequestMapper.selectByUserId(userId);
    }

    /** {@inheritDoc} */
    @Override
    public List<FriendRequest> selectAll() {
        return friendRequestMapper.selectAll();
    }

    /** {@inheritDoc} */
    @Override
    public List<FriendRequest> selectInbox(Long touserId, Integer status) {
        return friendRequestMapper.selectInbox(touserId, status);
    }

    /**
     * {@inheritDoc}
     *
     * <p>把 Mapper 的命中条数换算成布尔值：大于 0 即存在待处理申请。
     */
    @Override
    public boolean selectPending( Long fromUserId,  Long toUserId) {
        int rows = friendRequestMapper.selectPending(fromUserId, toUserId);
        if(rows > 0) {
            return true;
        } else {
            return false;
        }
    }
}

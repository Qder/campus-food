package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.entity.UserFeedPost;
import com.campus.food.campusfood.mapper.UserFeedPostMapper;
import com.campus.food.campusfood.service.UserFeedPostService;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户美食动态（Feed）服务实现。
 *
 * <p>当前为直接透传 Mapper 的薄封装；空 ID 集合在入口处短路，避免发出
 * {@code WHERE id IN ()} 这类无意义 SQL。
 *
 * @author yang
 * @since 2026-10-23
 */
@Service
public class UserFeedPostServiceImpl implements UserFeedPostService {

    private final UserFeedPostMapper userFeedPostMapper;

    /**
     * 构造注入 Mapper。
     *
     * @param userFeedPostMapper 动态 Mapper
     */
    public UserFeedPostServiceImpl(UserFeedPostMapper userFeedPostMapper) {
        this.userFeedPostMapper = userFeedPostMapper;
    }

    /** {@inheritDoc} */
    @Override
    public int insert(UserFeedPost userFeedPost) {
        return userFeedPostMapper.insert(userFeedPost);
    }

    /** {@inheritDoc} */
    @Override
    public int update(UserFeedPost userFeedPost) {
        return userFeedPostMapper.update(userFeedPost);
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Long id) {
        return userFeedPostMapper.delete(id);
    }

    /** {@inheritDoc} */
    @Override
    public UserFeedPost selectById(Long id) {
        return userFeedPostMapper.selectById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<UserFeedPost> selectByUserId(Long userId) {
        return userFeedPostMapper.selectByUserId(userId);
    }

    /** {@inheritDoc} */
    @Override
    public List<UserFeedPost> selectAll() {
        return userFeedPostMapper.selectAll();
    }

    /**
     * {@inheritDoc}
     *
     * <p>空集合直接返回空列表，不再访问数据库。
     */
    @Override
    public List<UserFeedPost> selectByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userFeedPostMapper.selectByIds(ids);
    }
}

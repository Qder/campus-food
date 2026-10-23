package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.Friendship;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * {@code friendship} 表 MyBatis Mapper。
 *
 * <p>关系双向存储，常用索引（仅注释提示，不自动生成方法）：
 * - UNIQUE uk_friendship: user_id, friend_id → 判重与双向删除
 * - INDEX idx_friendship_friend: friend_id → 反查关注者
 *
 * @author yang
 * @since 2026-10-23
 */
@Mapper
public interface FriendshipMapper {

    /**
     * 新增单向好友关系。
     *
     * @param friendship 待插入的关系
     * @return 影响行数
     */
    int insert(Friendship friendship);

    /**
     * 按主键更新关系（动态 SQL，仅更新非 null 字段）。
     *
     * @param friendship 含主键的待更新关系
     * @return 影响行数
     */
    int update(Friendship friendship);

    /**
     * 按主键删除关系。
     *
     * @param id 关系 ID
     * @return 影响行数
     */
    int delete(Long id);

    /**
     * 按主键查询关系。
     *
     * @param id 关系 ID
     * @return 关系记录；不存在返回 null
     */
    Friendship selectById(Long id);

    /**
     * 查询某人关注的好友列表。
     *
     * @param userId 用户 ID
     * @return 关系列表；无数据返回空列表
     */
    List<Friendship> selectByUserId(Long userId);

    /**
     * 查询全部关系。
     *
     * @return 关系列表；无数据返回空列表
     */
    List<Friendship> selectAll();

    /**
     * 统计单向关系是否存在。
     *
     * @param userId   用户 ID
     * @param friendId 好友用户 ID
     * @return 命中条数：0 表示不是好友
     */
    int selectByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 删除单个方向的关系，双向删除时调用两次。
     *
     * @param userId   用户 ID
     * @param friendId 好友用户 ID
     * @return 影响行数
     */
    int deletePair(@Param("userId") Long userId, @Param("friendId") Long friendId);
}

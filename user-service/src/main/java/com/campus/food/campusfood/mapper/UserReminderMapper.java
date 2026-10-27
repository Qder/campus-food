package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.UserReminder;

/**
 * {@code user_reminder} 表 Mapper。按用户 + 提醒类型读写开关。
 *
 * @author yang
 * @since 2026-10-27
 */
@Mapper
public interface UserReminderMapper {

    /**
     * 查询当前用户全部提醒行。
     *
     * @param userId 用户 ID
     * @return 行列表；无数据返回空列表
     */
    List<UserReminder> selectByUserId(@Param("userId") Long userId);

    /**
     * 按用户 + 类型取一条（表无唯一键，取 id 最小的一行）。
     *
     * @param userId     用户 ID
     * @param remindType CAMPAIGN / FRIEND_FEED / ORDER
     * @return 行；不存在返回 null
     */
    UserReminder selectByUserAndType(@Param("userId") Long userId,
            @Param("remindType") String remindType);

    /**
     * 新增一条开关。
     *
     * @param row 含 userId、remindType、channel、enabled
     * @return 影响行数
     */
    int insert(UserReminder row);

    /**
     * 按用户 + 类型更新开关。
     *
     * @param userId     用户 ID
     * @param remindType 类型
     * @param enabled    0 关 / 1 开
     * @param channel    渠道，可为 null 表示不改
     * @return 影响行数
     */
    int updateEnabled(@Param("userId") Long userId,
            @Param("remindType") String remindType,
            @Param("enabled") Integer enabled,
            @Param("channel") String channel);
}

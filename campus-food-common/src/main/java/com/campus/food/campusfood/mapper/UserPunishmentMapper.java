package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.UserPunishment;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@code user_punishment} MyBatis Mapper
 *
 * @author yang
 * @since 2026-11-14
 */
@Mapper
public interface UserPunishmentMapper {

    /**
     * 新增一条处罚记录。
     *
     * @param entity 待插入的处罚记录
     * @return 影响行数
     */
    int insert(UserPunishment entity);
}

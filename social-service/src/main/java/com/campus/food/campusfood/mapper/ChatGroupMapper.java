package com.campus.food.campusfood.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.campus.food.campusfood.entity.ChatGroup;

/**
 * {@code chat_group} Mapper。建群时插入一行并回填主键。
 *
 * @author yang
 * @since 2026-11-19
 */
@Mapper
public interface ChatGroupMapper {

    /**
     * 新建群，回填自增 {@code id}；{@code created_at} 走表默认值。
     *
     * @param row 须带群名称、群主、人数上限与状态
     * @return 影响行数
     */
    int insert(ChatGroup row);
}

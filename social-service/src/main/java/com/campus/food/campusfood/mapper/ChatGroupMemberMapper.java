package com.campus.food.campusfood.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.campus.food.campusfood.entity.ChatGroupMember;

/**
 * {@code chat_group_member} Mapper。建群时群主与成员各插一行。
 *
 * @author yang
 * @since 2026-11-19
 */
@Mapper
public interface ChatGroupMemberMapper {

    /**
     * 加入群。建群时群主、其余成员各调一次。
     *
     * @param row 须带 groupId、userId 与角色
     * @return 影响行数
     */
    int insert(ChatGroupMember row);
}

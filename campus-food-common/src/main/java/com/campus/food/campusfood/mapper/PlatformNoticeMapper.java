package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.PlatformNotice;

/**
 * 平台公告持久层，对应表 {@code platform_notice}。
 *
 * @author yang
 * @since 2026-11-16
 */
public interface PlatformNoticeMapper {

    /**
     * 新增公告，主键回写到 {@code notice.id}。
     *
     * @param notice 公告实体，标题、正文、发布状态、发布人不能为空
     * @return 受影响行数
     */
    int insert(PlatformNotice notice);

    /**
     * 管理端公告列表，按创建时间倒序，最多 100 条。
     *
     * @param publishStatus 0 草稿 / 1 已发布，null 表示不过滤
     * @return 公告列表，无数据时为空列表
     */
    List<PlatformNotice> selectAll(@Param("publishStatus") Integer publishStatus);

    /**
     * 用户端已发布公告列表，按发布时间倒序，最多 100 条。
     *
     * @return 已发布公告列表，无数据时为空列表
     */
    List<PlatformNotice> selectPublished();
}
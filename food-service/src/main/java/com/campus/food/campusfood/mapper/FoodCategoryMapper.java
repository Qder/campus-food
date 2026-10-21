package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.FoodCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * {@code food_category} MyBatis Mapper
 *
 * <p>常用查询键（来自 UNIQUE/INDEX，仅注释提示，不自动生成方法）：
 * - UNIQUE PRIMARY: id → 建议 selectById
 * - INDEX idx_fc_parent: parent_id → 建议 selectByParentId
 *
 * @author yang
 * @since 2026-10-16
 */
@Mapper
public interface FoodCategoryMapper {

    /**
     * 新增分类。
     *
     * @param entity 待插入的分类
     * @return 影响行数
     */
    int insert(FoodCategory entity);

    /**
     * 按主键查询分类。
     *
     * @param id 分类 ID
     * @return 分类记录；不存在返回 null
     */
    FoodCategory selectById(@Param("id") Integer id);

    /**
     * 查询所有启用中的分类。
     *
     * @return 启用分类列表；无数据返回空列表
     */
    List<FoodCategory> selectEnabled();

    /**
     * 按主键更新分类（动态 SQL，仅更新非 null 字段）。
     *
     * @param entity 含主键的待更新分类
     * @return 影响行数
     */
    int updateById(FoodCategory entity);
}

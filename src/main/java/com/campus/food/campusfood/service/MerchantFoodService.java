package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;

public interface MerchantFoodService {
    /**
     * 新增美食
     *
     * @param dto 美食信息
     * @return 美食ID
     */
    Long save(MerchantFoodSaveDTO dto);

    /**
     * 删除美食
     *
     * @param id 美食ID
     */
    void deleteById(Long id);

    /**
     * 更新美食
     *
     * @param id 美食ID
     * @param dto 美食信息
     * @return 更新行数
     * @throws RuntimeException 美食不存在
     */
    int updateById(Long id, MerchantFoodSaveDTO dto) throws RuntimeException;
}

package com.campus.food.campusfood.service;

import com.campus.food.campusfood.vo.UploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传服务。
 *
 * @author yang
 * @since 2026-11-15
 */
public interface UploadService {

    /**
     * 保存图片到本地磁盘。
     *
     * @param file    上传的文件，不能为空
     * @param bizType 业务类型：AVATAR / REVIEW / FOOD / MERCHANT_LOGO / LICENSE / FEED / CHAT
     * @return 相对访问地址与文件字节数
     * @throws com.campus.food.campusfood.exception.BizException
     *         文件为空、类型不支持、超过 5MB 或 bizType 非法时
     */
    UploadVO upload(MultipartFile file, String bizType);
}
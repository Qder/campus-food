package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.UploadService;
import com.campus.food.campusfood.vo.UploadVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传接口，需 JWT。
 *
 * @author yang
 * @since 2026-11-15
 */
@RestController
@RequestMapping("/user")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 上传图片。返回的 url 可直接传给改资料、发动态、写评价等接口。
     *
     * @param file    图片文件，字段名 file，必填
     * @param bizType 业务类型，必填
     * @return 相对访问地址与字节数
     * @throws BizException 文件为空、类型不支持、超过 5MB 或 bizType 非法时
     */
    @PostMapping("/upload")
    public UploadVO upload(@RequestParam(value = "file", required = false) MultipartFile file,
                           @RequestParam(value = "bizType", required = false) String bizType) {
        return uploadService.upload(file, bizType);
    }
}
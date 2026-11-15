package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.UploadService;
import com.campus.food.campusfood.vo.UploadVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 图片上传实现：校验类型与大小，按 业务类型/年/月/日 落盘，返回访问路径。
 *
 * <p>文件名只用 UUID，不拼接原始文件名，避免路径穿越和重名覆盖。
 *
 * @author yang
 * @since 2026-11-15
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    /** 允许的业务类型，同时决定一级目录名 */
    private static final Set<String> ALLOWED_BIZ_TYPES = Set.of(
            "AVATAR", "REVIEW", "FOOD", "MERCHANT_LOGO", "LICENSE", "FEED", "CHAT");

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif");

    /** 单文件上限 5MB */
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    /** 落盘日期目录格式 */
    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** 对外访问前缀 */
    private static final String URL_PREFIX = "/uploads/";

    /** 上传根目录，默认当前工作目录下的 uploads */
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * {@inheritDoc}
     *
     * <p>校验顺序：空文件 → bizType 白名单 → 大小 → 扩展名与 Content-Type。
     * 扩展名和 Content-Type 是两条独立校验，不要求彼此一致。
     */
    @Override
    public UploadVO upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        if (bizType == null || !ALLOWED_BIZ_TYPES.contains(bizType)) {
            throw new BizException("bizType 不合法");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException("图片不能超过 5MB");
        }

        String ext = extensionOf(file.getOriginalFilename());
        String contentType = file.getContentType();
        if (!ALLOWED_EXTENSIONS.contains(ext)
                || contentType == null
                || !contentType.startsWith("image/")) {
            throw new BizException("只支持 jpg/png/webp/gif 图片");
        }

        String relativePath = bizType.toLowerCase(Locale.ROOT) + "/"
                + LocalDate.now().format(DATE_PATH) + "/"
                + UUID.randomUUID().toString().replace("-", "") + "." + ext;

        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath);

        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(in, target);
        } catch (IOException e) {
            log.error("图片写入失败, userId={}, path={}", UserContext.getUserId(), relativePath, e);
            throw new BizException("图片上传失败，请稍后重试");
        }

        log.info("图片上传成功, userId={}, path={}, size={}",
                UserContext.getUserId(), relativePath, file.getSize());

        UploadVO vo = new UploadVO();
        vo.setUrl(URL_PREFIX + relativePath);
        vo.setSize(file.getSize());
        return vo;
    }

    /**
     * 取原始文件名的扩展名。
     *
     * @param originalFilename 原始文件名，可为 null
     * @return 小写扩展名；无扩展名时返回空串
     */
    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
package com.campus.food.campusfood.config;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 把本地上传目录映射为 {@code /uploads/**} 静态资源。
 *
 * @author yang
 * @since 2026-11-15
 */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    /** 上传根目录，与 UploadServiceImpl 使用同一配置项 */
    private final String uploadDir;

    /**
     * 构造器注入上传目录。
     *
     * @param uploadDir 上传根目录
     */
    public UploadResourceConfig(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * 注册静态资源映射。location 必须以斜杠结尾，否则会被当成文件处理。
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
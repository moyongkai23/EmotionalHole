package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    @Value("${file.upload.path:uploads/images}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/images/}")
    private String urlPrefix;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,webp}")
    private String allowedTypes;

    /** 上传图片：校验类型→生成UUID文件名→保存到本地→返回访问URL */
    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // 1. 验证文件是否为空
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("上传的文件不能为空");
            }

            // 2. 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("文件名不能为空");
            }

            String fileExtension = getFileExtension(originalFilename);
            List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
            if (!allowedTypeList.contains(fileExtension.toLowerCase())) {
                throw new IllegalArgumentException("不支持的文件类型，仅支持: " + allowedTypes);
            }

            // 3. 创建上传目录（使用绝对路径确保目录存在）
            String userDir = System.getProperty("user.dir");
            Path absoluteUploadDir = Paths.get(userDir, uploadPath);
            if (!Files.exists(absoluteUploadDir)) {
                Files.createDirectories(absoluteUploadDir);
                logger.info("创建上传目录: {}", absoluteUploadDir.toAbsolutePath());
            }

            // 4. 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExtension;
            Path filePath = absoluteUploadDir.resolve(uniqueFileName);

            // 5. 保存文件
            File destFile = filePath.toFile();
            file.transferTo(destFile);
            logger.info("文件上传成功: {}", filePath.toAbsolutePath());

            // 6. 返回访问URL
            return urlPrefix + uniqueFileName;

        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        throw new IllegalArgumentException("无法获取文件扩展名");
    }
}
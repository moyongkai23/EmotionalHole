package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传图片接口
     * @param file 上传的图片文件
     * @return 上传结果，包含图片访问URL
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 参数校验
            if (file == null || file.isEmpty()) {
                return Result.error(400, "上传的文件不能为空");
            }

            // 调用service层上传文件
            String imageUrl = fileUploadService.uploadImage(file);
            return Result.success(imageUrl);

        } catch (IllegalArgumentException e) {
            logger.warn("图片上传参数错误: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            logger.error("图片上传失败: {}", e.getMessage(), e);
            return Result.error(500, "图片上传失败，请重试");
        }
    }

    /**
     * 批量上传图片接口
     * @param files 上传的图片文件数组
     * @return 上传结果，包含所有图片访问URL数组
     */
    @PostMapping("/images")
    public Result<java.util.List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            // 参数校验
            if (files == null || files.length == 0) {
                return Result.error(400, "上传的文件不能为空");
            }

            // 限制批量上传数量
            if (files.length > 9) {
                return Result.error(400, "最多只能上传9张图片");
            }

            // 批量上传
            java.util.List<String> imageUrls = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String imageUrl = fileUploadService.uploadImage(file);
                    imageUrls.add(imageUrl);
                }
            }

            return Result.success(imageUrls);

        } catch (IllegalArgumentException e) {
            logger.warn("批量图片上传参数错误: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            logger.error("批量图片上传失败: {}", e.getMessage(), e);
            return Result.error(500, "图片上传失败，请重试");
        }
    }
}

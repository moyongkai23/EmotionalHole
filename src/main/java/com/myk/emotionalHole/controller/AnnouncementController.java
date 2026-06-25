package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Announcement;
import com.myk.emotionalHole.entity.Admin;
import com.myk.emotionalHole.mapper.AdminMapper;
import com.myk.emotionalHole.service.AnnouncementService;
import com.myk.emotionalHole.util.AdminJwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 公告管理接口
 */
@RestController
@RequestMapping("announcement")
public class AnnouncementController {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementController.class);

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AdminJwtUtils adminJwtUtils;

    /**
     * 创建公告
     * @param announcement 公告对象
     * @return 创建结果
     */
    @PostMapping
    public Result<?> createAnnouncement(@RequestBody Announcement announcement, HttpServletRequest request) {
        logger.info("开始处理创建公告请求，请求参数: {}", announcement);
        try {
            // 验证参数
            if (announcement.getTitle() == null || announcement.getTitle().isEmpty()) {
                logger.warn("公告标题不能为空");
                return Result.error(400, "公告标题不能为空");
            }
            if (announcement.getContentText() == null || announcement.getContentText().isEmpty()) {
                logger.warn("公告内容不能为空");
                return Result.error(400, "公告内容不能为空");
            }
            
            // 从请求中获取当前登录管理员信息
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String adminAccount = adminJwtUtils.extractAdminAccount(token);
                if (adminAccount != null) {
                    Admin admin = adminMapper.getAdminByAccount(adminAccount);
                    if (admin != null) {
                        announcement.setAdminId(admin.getId());
                        logger.info("获取当前管理员ID: {}", admin.getId());
                    }
                }
            }

            // 调用service层创建公告
            boolean result = announcementService.createAnnouncement(announcement);
            logger.info("创建公告请求处理完成，结果: {}", result);

            if (result) {
                return Result.success("公告创建成功");
            } else {
                return Result.error(500, "公告创建失败");
            }
        } catch (Exception e) {
            logger.error("创建公告请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 更新公告
     * @param announcement 公告对象
     * @return 更新结果
     */
    @PutMapping
    public Result<?> updateAnnouncement(@RequestBody Announcement announcement) {
        logger.info("开始处理更新公告请求，请求参数: {}", announcement);
        try {
            // 验证参数
            if (announcement.getId() == null) {
                logger.warn("公告ID不能为空");
                return Result.error(400, "公告ID不能为空");
            }
            if (announcement.getTitle() == null || announcement.getTitle().isEmpty()) {
                logger.warn("公告标题不能为空");
                return Result.error(400, "公告标题不能为空");
            }
            if (announcement.getContentText() == null || announcement.getContentText().isEmpty()) {
                logger.warn("公告内容不能为空");
                return Result.error(400, "公告内容不能为空");
            }

            // 调用service层更新公告
            boolean result = announcementService.updateAnnouncement(announcement);
            logger.info("更新公告请求处理完成，结果: {}", result);

            if (result) {
                return Result.success("公告更新成功");
            } else {
                return Result.error(500, "公告更新失败");
            }
        } catch (Exception e) {
            logger.error("更新公告请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 删除公告
     * @param id 公告ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        logger.info("开始处理删除公告请求，公告ID: {}", id);
        try {
            // 验证参数
            if (id == null) {
                logger.warn("公告ID不能为空");
                return Result.error(400, "公告ID不能为空");
            }

            // 调用service层删除公告
            boolean result = announcementService.deleteAnnouncement(id);
            logger.info("删除公告请求处理完成，结果: {}", result);

            if (result) {
                return Result.success("公告删除成功");
            } else {
                return Result.error(500, "公告删除失败");
            }
        } catch (Exception e) {
            logger.error("删除公告请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 获取公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        logger.info("开始处理获取公告详情请求，公告ID: {}", id);
        try {
            // 验证参数
            if (id == null) {
                logger.warn("公告ID不能为空");
                return Result.error(400, "公告ID不能为空");
            }

            // 调用service层获取公告详情
            Announcement announcement = announcementService.getAnnouncementById(id);
            logger.info("获取公告详情请求处理完成，结果: {}", announcement);

            if (announcement != null) {
                return Result.success(announcement);
            } else {
                return Result.error(404, "公告不存在");
            }
        } catch (Exception e) {
            logger.error("获取公告详情请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 获取公告列表
     * @param announcementStatus 公告状态
     * @param announcementType 公告类型
     * @param isPinned 是否置顶
     * @param page 页码
     * @param pageSize 每页大小
     * @return 公告列表
     */
    @GetMapping("/list")
    public Result<List<Announcement>> getAnnouncementList(
            @RequestParam(required = false) Integer announcementStatus,
            @RequestParam(required = false) Integer announcementType,
            @RequestParam(required = false) Integer isPinned,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 验证参数
            if (page < 1) {
                page = 1;
            }
            if (pageSize < 1 || pageSize > 100) {
                pageSize = 10;
            }

            // 调用service层获取公告列表
            List<Announcement> announcementList = announcementService.getAnnouncementList(announcementStatus, announcementType, isPinned, page, pageSize);

            return Result.success(announcementList);
        } catch (Exception e) {
            logger.error("获取公告列表请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 更新公告置顶状态
     * @param id 公告ID
     * @param isPinned 是否置顶
     * @return 更新结果
     */
    @PutMapping("/{id}/pinned")
    public Result<?> updatePinnedStatus(@PathVariable Long id, @RequestParam Integer isPinned) {
        logger.info("开始处理更新公告置顶状态请求，公告ID: {}, isPinned: {}", id, isPinned);
        try {
            // 验证参数
            if (id == null) {
                logger.warn("公告ID不能为空");
                return Result.error(400, "公告ID不能为空");
            }
            if (isPinned == null || (isPinned != 0 && isPinned != 1)) {
                logger.warn("置顶状态参数无效");
                return Result.error(400, "置顶状态参数无效，只能为0或1");
            }

            // 调用service层更新置顶状态
            boolean result = announcementService.updatePinnedStatus(id, isPinned);
            logger.info("更新公告置顶状态请求处理完成，结果: {}", result);

            if (result) {
                return Result.success(isPinned == 1 ? "公告置顶成功" : "公告取消置顶成功");
            } else {
                return Result.error(500, "更新置顶状态失败");
            }
        } catch (Exception e) {
            logger.error("更新公告置顶状态请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 更新公告状态
     * @param id 公告ID
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        logger.info("开始处理更新公告状态请求，公告ID: {}, status: {}", id, status);
        try {
            // 验证参数
            if (id == null) {
                logger.warn("公告ID不能为空");
                return Result.error(400, "公告ID不能为空");
            }
            if (status == null) {
                logger.warn("状态参数不能为空");
                return Result.error(400, "状态参数不能为空");
            }

            // 调用service层更新状态
            boolean result = announcementService.updateStatus(id, status);
            logger.info("更新公告状态请求处理完成，结果: {}", result);

            if (result) {
                return Result.success("公告状态更新成功");
            } else {
                return Result.error(500, "公告状态更新失败");
            }
        } catch (Exception e) {
            logger.error("更新公告状态请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }
}

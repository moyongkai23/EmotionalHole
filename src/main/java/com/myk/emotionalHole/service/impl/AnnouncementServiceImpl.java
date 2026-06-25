package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.entity.Announcement;
import com.myk.emotionalHole.mapper.AnnouncementMapper;
import com.myk.emotionalHole.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务实现类
 * 提供公告的增删改查、置顶、状态管理功能
 */
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public boolean createAnnouncement(Announcement announcement) {
        // 参数校验
        if (announcement.getAdminId() == null) {
            throw new IllegalArgumentException("管理员ID不能为空");
        }
        if (announcement.getIsPinned() == null) {
            throw new IllegalArgumentException("是否置顶不能为空");
        }
        if (announcement.getAnnouncementStatus() == null) {
            throw new IllegalArgumentException("公告状态不能为空");
        }
        // 设置创建时间和更新时间
        announcement.setCreateTime(java.time.LocalDateTime.now().toString());
        announcement.setUpdateTime(java.time.LocalDateTime.now().toString());
        return announcementMapper.insert(announcement) > 0;
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        // 更新时间
        announcement.setUpdateTime(java.time.LocalDateTime.now().toString());
        return announcementMapper.update(announcement) > 0;
    }

    @Override
    public boolean deleteAnnouncement(Long id) {
        return announcementMapper.delete(id) > 0;
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }

    @Override
    public List<Announcement> getAnnouncementList(Integer announcementStatus, Integer announcementType, Integer isPinned, int page, int pageSize) {
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        return announcementMapper.selectList(announcementStatus, announcementType, isPinned, offset, pageSize);
    }

    @Override
    public boolean updatePinnedStatus(Long id, Integer isPinned) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return false;
        }
        announcement.setIsPinned(isPinned);
        announcement.setUpdateTime(java.time.LocalDateTime.now().toString());
        return announcementMapper.update(announcement) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return false;
        }
        announcement.setAnnouncementStatus(status);
        announcement.setUpdateTime(java.time.LocalDateTime.now().toString());
        return announcementMapper.update(announcement) > 0;
    }
}
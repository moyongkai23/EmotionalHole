package com.myk.emotionalHole.service;

import com.myk.emotionalHole.entity.Announcement;
import java.util.List;

public interface AnnouncementService {

    /**
     * 创建公告
     * @param announcement 公告对象
     * @return 是否创建成功
     */
    boolean createAnnouncement(Announcement announcement);

    /**
     * 更新公告
     * @param announcement 公告对象
     * @return 是否更新成功
     */
    boolean updateAnnouncement(Announcement announcement);

    /**
     * 删除公告
     * @param id 公告ID
     * @return 是否删除成功
     */
    boolean deleteAnnouncement(Long id);

    /**
     * 根据ID获取公告
     * @param id 公告ID
     * @return 公告对象
     */
    Announcement getAnnouncementById(Long id);

    /**
     * 获取公告列表
     * @param announcementStatus 公告状态
     * @param announcementType 公告类型
     * @param isPinned 是否置顶
     * @param page 页码
     * @param pageSize 每页大小
     * @return 公告列表
     */
    List<Announcement> getAnnouncementList(Integer announcementStatus, Integer announcementType, Integer isPinned, int page, int pageSize);

    /**
     * 置顶/取消置顶公告
     * @param id 公告ID
     * @param isPinned 是否置顶
     * @return 是否操作成功
     */
    boolean updatePinnedStatus(Long id, Integer isPinned);

    /**
     * 更新公告状态
     * @param id 公告ID
     * @param status 状态
     * @return 是否操作成功
     */
    boolean updateStatus(Long id, Integer status);
}
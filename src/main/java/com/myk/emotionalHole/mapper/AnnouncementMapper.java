
package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    int insert(Announcement announcement);

    int update(Announcement announcement);

    int delete(Long id);

    Announcement selectById(Long id);

    List<Announcement> selectList(@Param("announcementStatus") Integer announcementStatus,
                                    @Param("announcementType") Integer announcementType,
                                    @Param("isPinned") Integer isPinned,
                                    @Param("offset") int offset,
                                    @Param("pageSize") int pageSize);
}

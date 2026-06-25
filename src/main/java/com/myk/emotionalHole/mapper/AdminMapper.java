package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMapper {
    /**
     * 根据管理员账号查询管理员信息
     * @param adminAccount 管理员账号
     * @return 管理员信息
     */
    Admin getAdminByAccount(String adminAccount);

    /**
     * 根据管理员ID查询管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    Admin getAdminById(Long id);

    /**
     * 插入管理员操作日志
     * @param admin 管理员信息
     * @return 影响行数
     */
    int insertAdmin(Admin admin);

    /**
     * 更新管理员信息
     * @param admin 管理员信息
     * @return 影响行数
     */
    int updateAdmin(Admin admin);

    /**
     * 查询所有管理员列表
     * @return 管理员列表
     */
    List<Admin> getAllAdmins();

    /**
     * 删除管理员
     * @param id 管理员ID
     * @return 影响行数
     */
    int deleteAdmin(Long id);
}

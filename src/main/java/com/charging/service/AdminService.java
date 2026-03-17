package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.AnnouncementRequest;
import com.charging.vo.AnnouncementVO;
import com.charging.vo.UserManageVO;

public interface AdminService {

    Page<UserManageVO> listUsers(String role, Integer status, String keyword, int page, int size);

    void updateUserStatus(Long userId, Integer status);

    Page<UserManageVO> listOperators(String keyword, int page, int size);

    Page<AnnouncementVO> listAnnouncements(int page, int size);

    void createAnnouncement(Long adminId, AnnouncementRequest request);

    void updateAnnouncement(Long id, AnnouncementRequest request);

    void offlineAnnouncement(Long id);
}

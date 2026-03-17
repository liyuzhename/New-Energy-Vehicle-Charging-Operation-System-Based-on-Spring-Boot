package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.AnnouncementRequest;
import com.charging.entity.Announcement;
import com.charging.entity.ChargingStation;
import com.charging.entity.User;
import com.charging.mapper.AnnouncementMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.UserMapper;
import com.charging.service.AdminService;
import com.charging.vo.AnnouncementVO;
import com.charging.vo.UserManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final AnnouncementMapper announcementMapper;
    private final ChargingStationMapper stationMapper;

    @Override
    public Page<UserManageVO> listUsers(String role, Integer status, String keyword, int page, int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);
        if (role != null && !role.isEmpty()) wrapper.eq(User::getRole, role);
        if (status != null) wrapper.eq(User::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getPhone, keyword));
        }
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        Page<UserManageVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(u -> {
            UserManageVO vo = new UserManageVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public Page<UserManageVO> listOperators(String keyword, int page, int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "OPERATOR")
                .orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getEmail, keyword));
        }
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        Page<UserManageVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(u -> {
            UserManageVO vo = new UserManageVO();
            BeanUtils.copyProperties(u, vo);
            long stationCount = stationMapper.selectCount(
                    new LambdaQueryWrapper<ChargingStation>().eq(ChargingStation::getOperatorId, u.getId()));
            vo.setStationCount(stationCount);
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public Page<AnnouncementVO> listAnnouncements(int page, int size) {
        Page<Announcement> aPage = announcementMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime));
        Page<AnnouncementVO> voPage = new Page<>(aPage.getCurrent(), aPage.getSize(), aPage.getTotal());
        voPage.setRecords(aPage.getRecords().stream().map(a -> {
            AnnouncementVO vo = new AnnouncementVO();
            BeanUtils.copyProperties(a, vo);
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public void createAnnouncement(Long adminId, AnnouncementRequest request) {
        Announcement a = new Announcement();
        a.setTitle(request.getTitle());
        a.setContent(request.getContent());
        a.setType(request.getType() != null ? request.getType() : "NOTICE");
        a.setStatus("ONLINE");
        a.setCreatorId(adminId);
        announcementMapper.insert(a);
    }

    @Override
    public void updateAnnouncement(Long id, AnnouncementRequest request) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "公告不存在");
        if (request.getTitle() != null) a.setTitle(request.getTitle());
        if (request.getContent() != null) a.setContent(request.getContent());
        if (request.getType() != null) a.setType(request.getType());
        announcementMapper.updateById(a);
    }

    @Override
    public void offlineAnnouncement(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "公告不存在");
        a.setStatus("OFFLINE");
        announcementMapper.updateById(a);
    }
}

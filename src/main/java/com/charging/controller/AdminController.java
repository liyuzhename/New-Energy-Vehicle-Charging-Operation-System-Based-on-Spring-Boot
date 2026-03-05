package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.dto.AnnouncementRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.AdminService;
import com.charging.service.OperationLogService;
import com.charging.vo.AnnouncementVO;
import com.charging.vo.OperationLogVO;
import com.charging.vo.UserManageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OperationLogService operationLogService;

    @GetMapping("/user/list")
    public Result<Page<UserManageVO>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.listUsers(role, status, keyword, page, size));
    }

    @PutMapping("/user/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return Result.success("用户状态已更新", null);
    }

    @GetMapping("/operator/list")
    public Result<Page<UserManageVO>> listOperators(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.listOperators(page, size));
    }

    @GetMapping("/announcement/list")
    public Result<Page<AnnouncementVO>> listAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.listAnnouncements(page, size));
    }

    @PostMapping("/announcement")
    public Result<Void> createAnnouncement(@Valid @RequestBody AnnouncementRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        adminService.createAnnouncement(adminId, request);
        return Result.success("公告发布成功", null);
    }

    @PutMapping("/announcement/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id,
                                           @RequestBody AnnouncementRequest request) {
        adminService.updateAnnouncement(id, request);
        return Result.success("公告更新成功", null);
    }

    @PutMapping("/announcement/{id}/offline")
    public Result<Void> offlineAnnouncement(@PathVariable Long id) {
        adminService.offlineAnnouncement(id);
        return Result.success("公告已下线", null);
    }

    @GetMapping("/log/list")
    public Result<Page<OperationLogVO>> listLogs(
            @RequestParam(required = false) String operatorName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(operationLogService.list(operatorName, page, size));
    }
}

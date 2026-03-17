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
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Integer statusInt = parseStatus(status);
        return Result.success(adminService.listUsers(role, statusInt, keyword, page, size));
    }

    @PutMapping("/user/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable("id") Long id,
                                         @RequestParam(value = "status") String status) {
        Integer statusInt = parseStatus(status);
        if (statusInt == null) return Result.error(400, "状态值不合法，请传 0（禁用）或 1（启用）");
        adminService.updateUserStatus(id, statusInt);
        return Result.success("用户状态已更新", null);
    }

    @GetMapping("/operator/list")
    public Result<Page<UserManageVO>> listOperators(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(adminService.listOperators(keyword, page, size));
    }

    private Integer parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        if ("0".equals(status) || "disabled".equalsIgnoreCase(status) || "禁用".equals(status)) return 0;
        if ("1".equals(status) || "enabled".equalsIgnoreCase(status) || "启用".equals(status)) return 1;
        try { return Integer.parseInt(status); } catch (NumberFormatException e) { return null; }
    }

    @GetMapping("/announcement/list")
    public Result<Page<AnnouncementVO>> listAnnouncements(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(adminService.listAnnouncements(page, size));
    }

    @PostMapping("/announcement")
    public Result<Void> createAnnouncement(@Valid @RequestBody AnnouncementRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        adminService.createAnnouncement(adminId, request);
        return Result.success("公告发布成功", null);
    }

    @PutMapping("/announcement/{id}")
    public Result<Void> updateAnnouncement(@PathVariable("id") Long id,
                                           @RequestBody AnnouncementRequest request) {
        adminService.updateAnnouncement(id, request);
        return Result.success("公告更新成功", null);
    }

    @PutMapping("/announcement/{id}/offline")
    public Result<Void> offlineAnnouncement(@PathVariable("id") Long id) {
        adminService.offlineAnnouncement(id);
        return Result.success("公告已下线", null);
    }

    @GetMapping("/log/list")
    public Result<Page<OperationLogVO>> listLogs(
            @RequestParam(value = "operatorName", required = false) String operatorName,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(operationLogService.list(operatorName, page, size));
    }
}

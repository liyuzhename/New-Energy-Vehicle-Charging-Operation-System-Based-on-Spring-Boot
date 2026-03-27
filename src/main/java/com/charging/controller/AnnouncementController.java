package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.service.AdminService;
import com.charging.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AdminService adminService;

    /**
     * 公开查询已上线公告（用户端/运营商端使用）
     */
    @GetMapping("/list")
    public Result<Page<AnnouncementVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        // 固定只查 ONLINE 状态的公告
        return Result.success(adminService.listAnnouncements(keyword, type, "ONLINE", page, size));
    }
}

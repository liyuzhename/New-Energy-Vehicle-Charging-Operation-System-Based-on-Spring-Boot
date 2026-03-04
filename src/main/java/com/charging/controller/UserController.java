package com.charging.controller;

import com.charging.common.result.Result;
import com.charging.dto.BindVehicleRequest;
import com.charging.dto.UpdatePasswordRequest;
import com.charging.dto.UpdateProfileRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.UserService;
import com.charging.vo.UserInfoVO;
import com.charging.vo.VehicleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(userId, request);
        return Result.success("修改成功", null);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updatePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    @GetMapping("/vehicles")
    public Result<List<VehicleVO>> getVehicles() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getVehicleList(userId));
    }

    @PostMapping("/vehicles")
    public Result<Void> bindVehicle(@Valid @RequestBody BindVehicleRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.bindVehicle(userId, request);
        return Result.success("车辆绑定成功", null);
    }

    @DeleteMapping("/vehicles/{id}")
    public Result<Void> unbindVehicle(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.unbindVehicle(userId, id);
        return Result.success("车辆解绑成功", null);
    }
}

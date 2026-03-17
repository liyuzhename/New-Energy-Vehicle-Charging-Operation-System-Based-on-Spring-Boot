package com.charging.service;

import com.charging.dto.*;
import com.charging.vo.LoginVO;
import com.charging.vo.UserInfoVO;
import com.charging.vo.VehicleVO;

import java.util.List;

public interface UserService {

    void register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    UserInfoVO getUserInfo(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void updatePassword(Long userId, UpdatePasswordRequest request);

    void bindVehicle(Long userId, BindVehicleRequest request);

    void unbindVehicle(Long userId, Long vehicleId);

    List<VehicleVO> getVehicleList(Long userId);
}

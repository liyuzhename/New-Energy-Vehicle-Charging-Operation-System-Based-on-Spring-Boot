package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.charging.common.exception.BusinessException;
import com.charging.dto.*;
import com.charging.entity.User;
import com.charging.entity.Vehicle;
import com.charging.entity.Wallet;
import com.charging.mapper.UserMapper;
import com.charging.mapper.VehicleMapper;
import com.charging.mapper.WalletMapper;
import com.charging.security.util.JwtUtils;
import com.charging.service.EmailService;
import com.charging.service.UserService;
import com.charging.vo.LoginVO;
import com.charging.vo.UserInfoVO;
import com.charging.vo.VehicleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final VehicleMapper vehicleMapper;
    private final WalletMapper walletMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private static final String RESET_CODE_PREFIX = "reset:code:";
    private static final long RESET_CODE_EXPIRE_MINUTES = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(400, "用户名已被使用");
        }
        if (userMapper.selectByPhone(request.getPhone()) != null) {
            throw new BusinessException(400, "手机号已被注册");
        }
        if (userMapper.selectByEmail(request.getEmail()) != null) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setTotalRecharge(BigDecimal.ZERO);
        wallet.setTotalConsume(BigDecimal.ZERO);
        walletMapper.insert(wallet);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        // 记住密码：7 天有效期；否则默认 1 天
        long expirationMs = request.isRememberMe() ? 7L * 24 * 60 * 60 * 1000 : 24L * 60 * 60 * 1000;
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole(), expirationMs);
        return new LoginVO(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            User existing = userMapper.selectByPhone(request.getPhone());
            if (existing != null && !existing.getId().equals(userId)) {
                throw new BusinessException(400, "手机号已被其他账号使用");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindVehicle(Long userId, BindVehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(userId);
        vehicle.setPlateNo(request.getPlateNo());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setBatteryCap(request.getBatteryCap());
        vehicleMapper.insert(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindVehicle(Long userId, Long vehicleId) {
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(404, "车辆不存在");
        }
        if (!vehicle.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该车辆");
        }
        vehicleMapper.deleteById(vehicleId);
    }

    @Override
    public List<VehicleVO> getVehicleList(Long userId) {
        List<Vehicle> vehicles = vehicleMapper.selectList(
                new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getUserId, userId)
        );
        return vehicles.stream().map(v -> {
            VehicleVO vo = new VehicleVO();
            BeanUtils.copyProperties(v, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userMapper.selectByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException(404, "该邮箱未注册");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(RESET_CODE_PREFIX + request.getEmail(), code,
                RESET_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        emailService.sendResetCode(request.getEmail(), code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        String key = RESET_CODE_PREFIX + request.getEmail();
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode == null) {
            throw new BusinessException(400, "验证码已过期，请重新发送");
        }
        if (!savedCode.equals(request.getCode())) {
            throw new BusinessException(400, "验证码错误");
        }
        User user = userMapper.selectByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        redisTemplate.delete(key);
    }
}

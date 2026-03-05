package com.charging.aspect;

import com.charging.entity.OperationLog;
import com.charging.entity.User;
import com.charging.mapper.UserMapper;
import com.charging.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final UserMapper userMapper;

    @Pointcut("execution(* com.charging.controller.Operator*.*(..)) || " +
              "execution(* com.charging.controller.Admin*.*(..)) || " +
              "execution(* com.charging.controller.OrderController.start(..)) || " +
              "execution(* com.charging.controller.OrderController.stop(..))")
    public void logPointcut() {}

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog opLog = new OperationLog();
        opLog.setCreateTime(LocalDateTime.now());
        opLog.setMethod(joinPoint.getSignature().toShortString());
        String paramsStr = Arrays.toString(joinPoint.getArgs());
        opLog.setParams(paramsStr.length() > 500 ? paramsStr.substring(0, 500) : paramsStr);

        // 获取当前用户
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long userId) {
                opLog.setOperatorId(userId);
                User user = userMapper.selectById(userId);
                if (user != null) opLog.setOperatorName(user.getUsername());
            }
        } catch (Exception ignored) {}

        // 获取请求IP
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                opLog.setIp(getClientIp(request));
                opLog.setOperation(request.getMethod() + " " + request.getRequestURI());
            }
        } catch (Exception ignored) {}

        try {
            Object result = joinPoint.proceed();
            opLog.setStatus(1);
            return result;
        } catch (Throwable ex) {
            opLog.setStatus(0);
            String msg = ex.getMessage();
            opLog.setErrorMsg(msg != null && msg.length() > 500 ? msg.substring(0, 500) : msg);
            throw ex;
        } finally {
            try {
                operationLogService.save(opLog);
            } catch (Exception e) {
                log.warn("操作日志保存失败", e);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        return ip;
    }
}

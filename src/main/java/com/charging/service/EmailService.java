package com.charging.service;

public interface EmailService {
    void sendResetCode(String toEmail, String code);
}

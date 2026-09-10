package com.tourist.service;

import com.tourist.dto.request.RegisterRequest;
import com.tourist.dto.response.LoginResponse;
import com.tourist.dto.response.UserProfileResponse;

public interface AuthService {

    LoginResponse login(String username, String password);

    /** 注册游客账号 */
    void register(RegisterRequest request);

    UserProfileResponse getCurrentUser(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    String forgotPassword(String username, String email);

    void resetPassword(String username, String newPassword);
}

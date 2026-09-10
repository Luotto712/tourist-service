package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.common.Result;
import com.tourist.dto.request.ChangePasswordRequest;
import com.tourist.dto.request.ForgotPasswordRequest;
import com.tourist.dto.request.LoginRequest;
import com.tourist.dto.request.RegisterRequest;
import com.tourist.dto.response.LoginResponse;
import com.tourist.dto.response.UserProfileResponse;
import com.tourist.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getUsername(), request.getPassword());
        return Result.success(response);
    }

    @GetMapping("/current-user")
    public Result<UserProfileResponse> getCurrentUser(@CurrentUser CurrentUserInfo currentUser) {
        UserProfileResponse userInfo = authService.getCurrentUser(currentUser.getUserId());
        return Result.success(userInfo);
    }

    @PutMapping("/change-password")
    public Result<String> changePassword(@CurrentUser CurrentUserInfo currentUser,
                                         @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUser.getUserId(), request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功");
    }

    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request.getUsername(), request.getEmail());
        return Result.success(message);
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody java.util.Map<String, String> body) {
        authService.resetPassword(body.get("username"), body.get("newPassword"));
        return Result.success("密码重置成功");
    }

}

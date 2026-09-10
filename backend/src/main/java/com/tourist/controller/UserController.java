package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.annotation.RequireRole;
import com.tourist.common.PageResult;
import com.tourist.common.Result;
import com.tourist.common.RoleConstants;
import com.tourist.dto.response.UserProfileResponse;
import com.tourist.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public Result<UserProfileResponse> getProfile(@CurrentUser CurrentUserInfo currentUser) {
        UserProfileResponse profile = userService.getProfile(currentUser.getUserId());
        return Result.success(profile);
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@CurrentUser CurrentUserInfo currentUser,
                                        @Valid @RequestBody UserProfileResponse request) {
        userService.updateProfile(currentUser.getUserId(), request);
        return Result.success("个人信息更新成功");
    }

    @GetMapping("/search")
    public Result<PageResult<UserProfileResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String college,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<UserProfileResponse> result = userService.searchUsers(keyword, role, college, page, pageSize);
        return Result.success(result);
    }

    @PutMapping("/{id}/status")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        userService.updateUserStatus(id, status);
        return Result.success(status != null && status == 1 ? "已启用" : "已禁用");
    }

}

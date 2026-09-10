package com.tourist.service;

import com.tourist.common.PageResult;
import com.tourist.dto.response.UserProfileResponse;
import com.tourist.entity.User;

public interface UserService {

    UserProfileResponse getProfile(Long userId);

    void updateProfile(Long userId, UserProfileResponse request);

    PageResult<UserProfileResponse> searchUsers(String keyword, String role, String college, int page, int pageSize);

    User getUserById(Long userId);

    void updateUserStatus(Long userId, Integer status);
}

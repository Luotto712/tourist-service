package com.tourist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourist.common.BusinessException;
import com.tourist.common.PageResult;
import com.tourist.common.ResultCode;
import com.tourist.dto.response.UserProfileResponse;
import com.tourist.entity.User;
import com.tourist.mapper.UserMapper;
import com.tourist.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return mapToProfile(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserProfileResponse request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public PageResult<UserProfileResponse> searchUsers(String keyword, String role, String college, int page, int pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentNo, keyword)
                    .or().like(User::getTeacherNo, keyword));
        }
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        if (college != null && !college.trim().isEmpty()) {
            wrapper.eq(User::getCollege, college);
        }
        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> iPage = userMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<UserProfileResponse> records = iPage.getRecords().stream()
                .map(this::mapToProfile)
                .collect(Collectors.toList());
        return PageResult.of(iPage.getTotal(), records);
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private UserProfileResponse mapToProfile(User user) {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setRealName(user.getRealName());
        profile.setRole(user.getRole());
        profile.setCollege(user.getCollege());
        profile.setStudentNo(user.getStudentNo());
        profile.setTeacherNo(user.getTeacherNo());
        profile.setGpa(user.getGpa());
        profile.setGradeScore(user.getGradeScore());
        profile.setPhone(user.getPhone());
        profile.setEmail(user.getEmail());
        profile.setStatus(user.getStatus());
        profile.setCreateTime(user.getCreateTime());
        profile.setUpdateTime(user.getUpdateTime());
        return profile;
    }
}

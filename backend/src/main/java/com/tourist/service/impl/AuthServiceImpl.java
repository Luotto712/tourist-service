package com.tourist.service.impl;

import com.tourist.common.BusinessException;
import com.tourist.common.ResultCode;
import com.tourist.config.JwtTokenProvider;
import com.tourist.dto.request.RegisterRequest;
import com.tourist.dto.response.LoginResponse;
import com.tourist.dto.response.UserProfileResponse;
import com.tourist.entity.User;
import com.tourist.mapper.UserMapper;
import com.tourist.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        User exists = userMapper.findByUsernameIncludeDisabled(request.getUsername());
        if (exists != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getUsername());
        user.setRole(com.tourist.common.RoleConstants.TOURIST);
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getCollege());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setCollege(user.getCollege());
        return response;
    }

    @Override
    public UserProfileResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return mapToProfile(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新密码长度不能小于6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public String forgotPassword(String username, String email) {
        User user = userMapper.findByUsername(username);
        if (user == null || email == null || !email.equals(user.getEmail())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请输入正确的邮箱或账号");
        }
        return "验证通过";
    }

    @Override
    @Transactional
    public void resetPassword(String username, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新密码长度不能小于6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
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

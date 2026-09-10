package com.tourist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourist.common.BusinessException;
import com.tourist.common.PageResult;
import com.tourist.common.ResultCode;
import com.tourist.dto.request.EmergencyInfoRequest;
import com.tourist.entity.EmergencyInfo;
import com.tourist.common.RoleConstants;
import com.tourist.entity.User;
import com.tourist.enums.NotificationType;
import com.tourist.mapper.EmergencyInfoMapper;
import com.tourist.mapper.UserMapper;
import com.tourist.service.EmergencyInfoService;
import com.tourist.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmergencyInfoServiceImpl implements EmergencyInfoService {

    @Resource
    private EmergencyInfoMapper emergencyInfoMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private NotificationService notificationService;

    @Override
    @Transactional
    public EmergencyInfo publish(Long publisherId, EmergencyInfoRequest request) {
        EmergencyInfo info = new EmergencyInfo();
        info.setTitle(request.getTitle());
        info.setContent(request.getContent());
        info.setValidFrom(request.getValidFrom());
        info.setValidTo(request.getValidTo());
        info.setStatus("PENDING");
        info.setPublisherId(publisherId);
        emergencyInfoMapper.insert(info);
        // 通知所有审批人员：有待审批的应急信息
        notifyRole(RoleConstants.APPROVER, "应急信息待审批",
                "您有一条应急通知待审批！", NotificationType.EMERGENCY_SUBMITTED, info.getId());
        return info;
    }

    @Override
    public PageResult<EmergencyInfo> adminList(int page, int pageSize) {
        LambdaQueryWrapper<EmergencyInfo> wrapper = new LambdaQueryWrapper<EmergencyInfo>()
                .orderByDesc(EmergencyInfo::getCreateTime);
        IPage<EmergencyInfo> iPage = emergencyInfoMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(iPage.getTotal(), enrich(iPage.getRecords()));
    }

    @Override
    public List<EmergencyInfo> publicList() {
        LocalDate today = LocalDate.now();
        List<EmergencyInfo> list = emergencyInfoMapper.selectList(new LambdaQueryWrapper<EmergencyInfo>()
                .eq(EmergencyInfo::getStatus, "APPROVED")
                .le(EmergencyInfo::getValidFrom, today)
                .ge(EmergencyInfo::getValidTo, today)
                .orderByDesc(EmergencyInfo::getPublishTime));
        return enrich(list);
    }

    @Override
    public List<EmergencyInfo> pendingList() {
        List<EmergencyInfo> list = emergencyInfoMapper.selectList(new LambdaQueryWrapper<EmergencyInfo>()
                .eq(EmergencyInfo::getStatus, "PENDING")
                .orderByAsc(EmergencyInfo::getCreateTime));
        return enrich(list);
    }

    @Override
    public EmergencyInfo detail(Long id) {
        EmergencyInfo info = emergencyInfoMapper.selectById(id);
        if (info == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "应急信息不存在");
        }
        List<EmergencyInfo> list = enrich(java.util.Collections.singletonList(info));
        return list.get(0);
    }

    @Override
    @Transactional
    public void update(Long id, EmergencyInfoRequest request) {
        EmergencyInfo info = require(id);
        info.setTitle(request.getTitle());
        info.setContent(request.getContent());
        info.setValidFrom(request.getValidFrom());
        info.setValidTo(request.getValidTo());
        info.setStatus("PENDING");
        info.setPublishTime(null);
        emergencyInfoMapper.updateById(info);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EmergencyInfo info = require(id);
        emergencyInfoMapper.deleteById(info.getId());
    }

    @Override
    @Transactional
    public void approve(Long id) {
        EmergencyInfo info = require(id);
        info.setStatus("APPROVED");
        info.setPublishTime(LocalDateTime.now());
        emergencyInfoMapper.updateById(info);
        // 通知所有游客：收到一条应急通知
        notifyRole(RoleConstants.TOURIST, "应急通知",
                "您收到了一条应急通知，请及时查看！", NotificationType.EMERGENCY_PUBLISHED, info.getId());
    }

    @Override
    @Transactional
    public void reject(Long id) {
        EmergencyInfo info = require(id);
        info.setStatus("REJECTED");
        emergencyInfoMapper.updateById(info);
        // 通知发布者：应急信息被驳回
        notificationService.sendNotification(info.getPublisherId(), "应急信息被驳回",
                "您发布的应急消息被驳回！", NotificationType.EMERGENCY_REJECTED.name(), info.getId());
    }

    // ---------------- helpers ----------------

    /** 给某角色的所有启用用户发通知 */
    private void notifyRole(String role, String title, String content, NotificationType type, Long relatedId) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, role).eq(User::getStatus, 1));
        for (User u : users) {
            notificationService.sendNotification(u.getId(), title, content, type.name(), relatedId);
        }
    }

    private EmergencyInfo require(Long id) {
        EmergencyInfo info = emergencyInfoMapper.selectById(id);
        if (info == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "应急信息不存在");
        }
        return info;
    }

    private List<EmergencyInfo> enrich(List<EmergencyInfo> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        Map<Long, String> names = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a));
        list.forEach(i -> i.setPublisherName(names.getOrDefault(i.getPublisherId(), "")));
        return list;
    }
}

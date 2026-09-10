package com.tourist.service;

import com.tourist.dto.request.EmergencyInfoRequest;
import com.tourist.entity.EmergencyInfo;
import com.tourist.entity.User;
import com.tourist.mapper.EmergencyInfoMapper;
import com.tourist.enums.NotificationType;
import com.tourist.mapper.UserMapper;
import com.tourist.service.impl.EmergencyInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 应急信息状态机（单元，Mockito）：发布/审批/驳回/修改回退/详情。 */
@ExtendWith(MockitoExtension.class)
class EmergencyInfoServiceImplTest {

    @Mock private EmergencyInfoMapper emergencyInfoMapper;
    @Mock private UserMapper userMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks private EmergencyInfoServiceImpl service;

    private EmergencyInfoRequest request() {
        EmergencyInfoRequest r = new EmergencyInfoRequest();
        r.setTitle("预警");
        r.setContent("内容");
        r.setValidFrom(LocalDate.of(2026, 9, 9));
        r.setValidTo(LocalDate.of(2026, 9, 30));
        return r;
    }

    private EmergencyInfo info(String status) {
        EmergencyInfo e = new EmergencyInfo();
        e.setId(1L);
        e.setStatus(status);
        e.setPublisherId(5L);
        return e;
    }

    @Test
    void publish_createsPending() {
        EmergencyInfo result = service.publish(5L, request());
        assertEquals("PENDING", result.getStatus());
        assertEquals(5L, result.getPublisherId());
    }

    @Test
    void approve_setsApprovedAndPublishTime() {
        when(emergencyInfoMapper.selectById(1L)).thenReturn(info("PENDING"));

        service.approve(1L);

        ArgumentCaptor<EmergencyInfo> cap = ArgumentCaptor.forClass(EmergencyInfo.class);
        verify(emergencyInfoMapper).updateById(cap.capture());
        assertEquals("APPROVED", cap.getValue().getStatus());
        assertNotNull(cap.getValue().getPublishTime());
    }

    @Test
    void reject_setsRejected() {
        when(emergencyInfoMapper.selectById(1L)).thenReturn(info("PENDING"));

        service.reject(1L);

        ArgumentCaptor<EmergencyInfo> cap = ArgumentCaptor.forClass(EmergencyInfo.class);
        verify(emergencyInfoMapper).updateById(cap.capture());
        assertEquals("REJECTED", cap.getValue().getStatus());
    }

    @Test
    void update_revertsToPending_andClearsPublishTime() {
        EmergencyInfo approved = info("APPROVED");
        approved.setPublishTime(java.time.LocalDateTime.now());
        when(emergencyInfoMapper.selectById(1L)).thenReturn(approved);

        service.update(1L, request());

        ArgumentCaptor<EmergencyInfo> cap = ArgumentCaptor.forClass(EmergencyInfo.class);
        verify(emergencyInfoMapper).updateById(cap.capture());
        assertEquals("PENDING", cap.getValue().getStatus());
        assertNull(cap.getValue().getPublishTime());
    }

    @Test
    void detail_enrichesPublisherName() {
        EmergencyInfo e = info("APPROVED");
        when(emergencyInfoMapper.selectById(1L)).thenReturn(e);
        User publisher = new User();
        publisher.setId(5L);
        publisher.setRealName("平台管理员");
        when(userMapper.selectList(any())).thenReturn(List.of(publisher));

        EmergencyInfo result = service.detail(1L);
        assertEquals("平台管理员", result.getPublisherName());
    }

    @Test
    void publish_notifiesAllApprovers() {
        User ap = new User(); ap.setId(2L); ap.setRole("APPROVER"); ap.setStatus(1);
        when(userMapper.selectList(any())).thenReturn(java.util.List.of(ap));

        service.publish(1L, request());

        verify(notificationService).sendNotification(eq(2L), anyString(), anyString(), eq(NotificationType.EMERGENCY_SUBMITTED.name()), any());
    }

    @Test
    void approve_notifiesTourists() {
        EmergencyInfo e = info("PENDING");
        when(emergencyInfoMapper.selectById(1L)).thenReturn(e);
        User t = new User(); t.setId(6L); t.setRole("TOURIST"); t.setStatus(1);
        when(userMapper.selectList(any())).thenReturn(java.util.List.of(t));

        service.approve(1L);

        verify(notificationService).sendNotification(eq(6L), anyString(), anyString(), eq(NotificationType.EMERGENCY_PUBLISHED.name()), eq(1L));
    }

    @Test
    void reject_notifiesPublisher() {
        EmergencyInfo e = info("PENDING");
        e.setPublisherId(1L);
        when(emergencyInfoMapper.selectById(1L)).thenReturn(e);

        service.reject(1L);

        verify(notificationService).sendNotification(eq(1L), anyString(), anyString(), eq(NotificationType.EMERGENCY_REJECTED.name()), eq(1L));
    }

}

package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourist.annotation.RequireRole;
import com.tourist.common.Result;
import com.tourist.common.RoleConstants;
import com.tourist.entity.EmergencyInfo;
import com.tourist.entity.TouristComplaint;
import com.tourist.enums.ComplaintStatus;
import com.tourist.mapper.EmergencyInfoMapper;
import com.tourist.mapper.NonstarHotelMapper;
import com.tourist.mapper.StarHotelMapper;
import com.tourist.mapper.TouristComplaintMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Resource
    private TouristComplaintMapper complaintMapper;

    @Resource
    private EmergencyInfoMapper emergencyInfoMapper;

    @Resource
    private StarHotelMapper starHotelMapper;

    @Resource
    private NonstarHotelMapper nonstarHotelMapper;

    /** 数据看板统计（平台管理员） */
    @GetMapping("/overview")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<Map<String, Long>> overview() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("totalComplaints", complaintMapper.selectCount(null));
        m.put("pendingComplaints", complaintMapper.selectCount(new LambdaQueryWrapper<TouristComplaint>()
                .eq(TouristComplaint::getStatus, ComplaintStatus.PENDING.name())));
        m.put("processingComplaints", complaintMapper.selectCount(new LambdaQueryWrapper<TouristComplaint>()
                .eq(TouristComplaint::getStatus, ComplaintStatus.PROCESSING.name())));
        m.put("closedComplaints", complaintMapper.selectCount(new LambdaQueryWrapper<TouristComplaint>()
                .eq(TouristComplaint::getStatus, ComplaintStatus.CLOSED.name())));
        m.put("totalEmergency", emergencyInfoMapper.selectCount(null));
        m.put("approvedEmergency", emergencyInfoMapper.selectCount(new LambdaQueryWrapper<EmergencyInfo>()
                .eq(EmergencyInfo::getStatus, "APPROVED")));
        m.put("totalStarHotels", starHotelMapper.selectCount(null));
        m.put("totalNonstarHotels", nonstarHotelMapper.selectCount(null));
        return Result.success(m);
    }

    /** 投诉按状态分布（数据看板图表） */
    @GetMapping("/complaint-status")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<Map<String, Long>> complaintStatus() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (ComplaintStatus st : ComplaintStatus.values()) {
            m.put(st.name(), complaintMapper.selectCount(new LambdaQueryWrapper<TouristComplaint>()
                    .eq(TouristComplaint::getStatus, st.name())));
        }
        return Result.success(m);
    }

    /** 应急信息按状态分布 */
    @GetMapping("/emergency-status")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<Map<String, Long>> emergencyStatus() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (String s : new String[]{"PENDING", "APPROVED", "REJECTED"}) {
            m.put(s, emergencyInfoMapper.selectCount(new LambdaQueryWrapper<EmergencyInfo>()
                    .eq(EmergencyInfo::getStatus, s)));
        }
        return Result.success(m);
    }
}

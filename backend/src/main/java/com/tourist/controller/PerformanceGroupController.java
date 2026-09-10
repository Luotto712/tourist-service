package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.PerformanceGroup;
import com.tourist.mapper.PerformanceGroupMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/performance-groups")
public class PerformanceGroupController extends BaseCatalogController<PerformanceGroup> {

    @Resource
    private PerformanceGroupMapper performanceGroupMapper;

    @Override
    protected BaseMapper<PerformanceGroup> mapper() {
        return performanceGroupMapper;
    }

    @Override
    protected LambdaQueryWrapper<PerformanceGroup> buildWrapper(String keyword) {
        LambdaQueryWrapper<PerformanceGroup> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(PerformanceGroup::getName, keyword);
        }
        w.orderByDesc(PerformanceGroup::getCreateTime);
        return w;
    }
}

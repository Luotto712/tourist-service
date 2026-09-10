package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.TourRoute;
import com.tourist.mapper.TourRouteMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/routes")
public class TourRouteController extends BaseCatalogController<TourRoute> {

    @Resource
    private TourRouteMapper tourRouteMapper;

    @Override
    protected BaseMapper<TourRoute> mapper() {
        return tourRouteMapper;
    }

    @Override
    protected LambdaQueryWrapper<TourRoute> buildWrapper(String keyword) {
        LambdaQueryWrapper<TourRoute> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(TourRoute::getName, keyword);
        }
        w.orderByDesc(TourRoute::getCreateTime);
        return w;
    }
}

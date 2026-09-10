package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.Attraction;
import com.tourist.mapper.AttractionMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController extends BaseCatalogController<Attraction> {

    @Resource
    private AttractionMapper attractionMapper;

    @Override
    protected BaseMapper<Attraction> mapper() {
        return attractionMapper;
    }

    @Override
    protected LambdaQueryWrapper<Attraction> buildWrapper(String keyword) {
        LambdaQueryWrapper<Attraction> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(Attraction::getName, keyword);
        }
        w.orderByDesc(Attraction::getCreateTime);
        return w;
    }
}

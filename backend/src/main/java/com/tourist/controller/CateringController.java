package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.Catering;
import com.tourist.mapper.CateringMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/catering")
public class CateringController extends BaseCatalogController<Catering> {

    @Resource
    private CateringMapper cateringMapper;

    @Override
    protected BaseMapper<Catering> mapper() {
        return cateringMapper;
    }

    @Override
    protected LambdaQueryWrapper<Catering> buildWrapper(String keyword) {
        LambdaQueryWrapper<Catering> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(Catering::getName, keyword);
        }
        w.orderByDesc(Catering::getCreateTime);
        return w;
    }
}

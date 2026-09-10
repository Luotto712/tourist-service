package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.TransportInfo;
import com.tourist.mapper.TransportInfoMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/transport")
public class TransportInfoController extends BaseCatalogController<TransportInfo> {

    @Resource
    private TransportInfoMapper transportInfoMapper;

    @Override
    protected BaseMapper<TransportInfo> mapper() {
        return transportInfoMapper;
    }

    @Override
    protected LambdaQueryWrapper<TransportInfo> buildWrapper(String keyword) {
        LambdaQueryWrapper<TransportInfo> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(TransportInfo::getName, keyword);
        }
        w.orderByDesc(TransportInfo::getCreateTime);
        return w;
    }
}

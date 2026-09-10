package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.NonstarHotel;
import com.tourist.mapper.NonstarHotelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/hotels/nonstar")
public class NonstarHotelController extends BaseCatalogController<NonstarHotel> {

    @Resource
    private NonstarHotelMapper nonstarHotelMapper;

    @Override
    protected BaseMapper<NonstarHotel> mapper() {
        return nonstarHotelMapper;
    }

    @Override
    protected LambdaQueryWrapper<NonstarHotel> buildWrapper(String keyword) {
        LambdaQueryWrapper<NonstarHotel> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(NonstarHotel::getName, keyword);
        }
        w.orderByDesc(NonstarHotel::getCreateTime);
        return w;
    }
}

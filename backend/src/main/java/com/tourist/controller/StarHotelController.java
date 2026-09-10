package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.StarHotel;
import com.tourist.mapper.StarHotelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/hotels/star")
public class StarHotelController extends BaseCatalogController<StarHotel> {

    @Resource
    private StarHotelMapper starHotelMapper;

    @Override
    protected BaseMapper<StarHotel> mapper() {
        return starHotelMapper;
    }

    @Override
    protected LambdaQueryWrapper<StarHotel> buildWrapper(String keyword) {
        LambdaQueryWrapper<StarHotel> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(StarHotel::getName, keyword);
        }
        w.orderByDesc(StarHotel::getCreateTime);
        return w;
    }
}

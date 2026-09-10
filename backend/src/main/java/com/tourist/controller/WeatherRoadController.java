package com.tourist.controller;

import com.tourist.annotation.RequireRole;
import com.tourist.client.AmapClient;
import com.tourist.common.Result;
import com.tourist.common.RoleConstants;
import com.tourist.dto.response.RoadInfoResponse;
import com.tourist.dto.response.WeatherInfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WeatherRoadController {

    @Resource
    private AmapClient amapClient;

    @GetMapping("/weather")
    @RequireRole(RoleConstants.TOURIST)
    public Result<WeatherInfoResponse> weather() {
        return Result.success(amapClient.getWeather());
    }

    @GetMapping("/road-conditions")
    @RequireRole(RoleConstants.TOURIST)
    public Result<List<RoadInfoResponse>> road() {
        return Result.success(amapClient.getRoad());
    }
}

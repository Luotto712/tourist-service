package com.tourist.client;

import com.tourist.dto.response.RoadInfoResponse;
import com.tourist.dto.response.WeatherInfoResponse;

import java.util.List;

/**
 * 高德地图 Web 服务端口（真外部依赖）。
 * 无 key 或调用失败时，实现走 mock 降级；口径为成都市武侯区。
 */
public interface AmapClient {

    WeatherInfoResponse getWeather();

    List<RoadInfoResponse> getRoad();
}

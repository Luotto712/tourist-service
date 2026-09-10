package com.tourist.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourist.config.AmapProperties;
import com.tourist.dto.response.RoadInfoResponse;
import com.tourist.dto.response.WeatherInfoResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 高德 Web 服务实现（restapi.amap.com）。
 * - 天气：GET /v3/weather/weatherInfo?city=510107&extensions=base（武侯区 adcode，实况 lives[]）
 * - 路况：GET /v3/traffic/status/rectangle（武侯祠片区包围盒；trafficinfo.roads[].status=1/2/3 → 畅通/缓慢/拥堵）
 * 无 key / 异常时降级为本地 mock；结果按 cache-minutes 缓存。
 */
@Component
public class AmapClientImpl implements AmapClient {

    private static final String AREA = "成都市武侯区";
    private static final String CITY_ADCODE = "510107";
    private static final String TRAFFIC_RECTANGLE = "104.03,30.60;104.10,30.66"; // 武侯祠片区包围盒

    @Resource
    private AmapProperties props;

    private RestTemplate rest;
    private ObjectMapper mapper;

    private volatile WeatherInfoResponse weather;
    private volatile long weatherAt;

    private volatile List<RoadInfoResponse> road;
    private volatile long roadAt;

    @PostConstruct
    void init() {
        this.rest = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public WeatherInfoResponse getWeather() {
        if (weather != null && fresh(weatherAt)) {
            return weather;
        }
        WeatherInfoResponse v = fetchWeather();
        weather = v;
        weatherAt = System.currentTimeMillis();
        return v;
    }

    @Override
    public List<RoadInfoResponse> getRoad() {
        if (road != null && fresh(roadAt)) {
            return road;
        }
        List<RoadInfoResponse> v = fetchRoad();
        road = v;
        roadAt = System.currentTimeMillis();
        return v;
    }

    // ---------------- fetch ----------------

    @SuppressWarnings("unchecked")
    private WeatherInfoResponse fetchWeather() {
        if (noKey()) {
            return mockWeather();
        }
        try {
            Map<String, Object> resp = rest.getForObject(
                    UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/weather/weatherInfo")
                            .queryParam("key", props.getKey())
                            .queryParam("city", CITY_ADCODE)
                            .queryParam("extensions", "base")
                            .build(true).toUri(), Map.class);
            List<Object> lives = (List<Object>) resp.get("lives");
            if (lives != null && !lives.isEmpty()) {
                Map<String, Object> live = (Map<String, Object>) lives.get(0);
                WeatherInfoResponse w = new WeatherInfoResponse();
                w.setArea(AREA);
                w.setWeather(str(live.get("weather")));
                w.setTemp(str(live.get("temperature")));
                w.setWind(str(live.get("winddirection")) + str(live.get("windpower")));
                String report = str(live.get("reporttime"));
                w.setDate(report.length() >= 10 ? report.substring(0, 10) : report);
                return w;
            }
            return mockWeather();
        } catch (Exception e) {
            return mockWeather();
        }
    }

    @SuppressWarnings("unchecked")
    private List<RoadInfoResponse> fetchRoad() {
        if (noKey()) {
            return mockRoad();
        }
        try {
            Map<String, Object> resp = rest.getForObject(
                    UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/traffic/status/rectangle")
                            .queryParam("key", props.getKey())
                            .queryParam("rectangle", TRAFFIC_RECTANGLE)
                            .queryParam("extensions", "all")
                            .build(true).toUri(), Map.class);
            Map<String, Object> trafficInfo = (Map<String, Object>) resp.get("trafficinfo");
            List<Object> roads = (List<Object>) trafficInfo.get("roads");
            if (roads == null || roads.isEmpty()) {
                return mockRoad();
            }
            List<RoadInfoResponse> out = new ArrayList<>();
            for (int i = 0; i < Math.min(roads.size(), 6); i++) {
                Map<String, Object> r = (Map<String, Object>) roads.get(i);
                RoadInfoResponse ri = new RoadInfoResponse();
                ri.setArea(AREA);
                ri.setRoad(str(r.get("name")));
                ri.setCondition(statusText(r.get("status")));
                ri.setNote("实时路况来自高德地图");
                out.add(ri);
            }
            return out;
        } catch (Exception e) {
            return mockRoad();
        }
    }

    /** 高德路况 status 编码 → 文案：1畅通 / 2缓慢 / 3拥堵 / 4严重拥堵 */
    String statusText(Object status) {
        switch (str(status)) {
            case "1": return "畅通";
            case "2": return "缓慢";
            case "3": return "拥堵";
            case "4": return "严重拥堵";
            default: return "未知";
        }
    }

    // ---------------- mock / helpers ----------------

    private boolean noKey() {
        return props.getKey() == null || props.getKey().isEmpty();
    }

    private boolean fresh(long at) {
        return System.currentTimeMillis() - at < props.getCacheMinutes() * 60_000L;
    }

    private WeatherInfoResponse mockWeather() {
        WeatherInfoResponse w = new WeatherInfoResponse();
        w.setArea(AREA);
        w.setDate(java.time.LocalDate.now().toString());
        w.setWeather("多云");
        w.setTemp("26");
        w.setWind("东北风2级");
        return w;
    }

    private List<RoadInfoResponse> mockRoad() {
        List<RoadInfoResponse> list = new ArrayList<>();
        RoadInfoResponse r1 = new RoadInfoResponse();
        r1.setArea(AREA);
        r1.setRoad("武侯祠大街");
        r1.setCondition("畅通");
        r1.setNote("本地演示数据，接入高德 key 后为实时路况");
        list.add(r1);
        RoadInfoResponse r2 = new RoadInfoResponse();
        r2.setArea(AREA);
        r2.setRoad("一环路南三段");
        r2.setCondition("缓行");
        r2.setNote("本地演示数据，接入高德 key 后为实时路况");
        list.add(r2);
        return list;
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }
}

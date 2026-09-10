package com.tourist.client;

import com.tourist.config.AmapProperties;
import com.tourist.dto.response.RoadInfoResponse;
import com.tourist.dto.response.WeatherInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Amap 客户端：无 key 时降级 mock + 路况 status 数值 → 文案映射。 */
class AmapClientImplTest {

    private AmapClientImpl client;

    @BeforeEach
    void setUp() {
        client = new AmapClientImpl();
        AmapProperties props = new AmapProperties();
        props.setKey("");
        props.setTimeoutMs(5000);
        props.setCacheMinutes(10);
        ReflectionTestUtils.setField(client, "props", props);
        client.init();
    }

    @Test
    void getWeather_withoutKey_returnsMock() {
        WeatherInfoResponse w = client.getWeather();
        assertNotNull(w);
        assertEquals("成都市武侯区", w.getArea());
        assertNotNull(w.getWeather());
    }

    @Test
    void getRoad_withoutKey_returnsMockList() {
        List<RoadInfoResponse> road = client.getRoad();
        assertNotNull(road);
        assertTrue(road.size() >= 1);
        assertEquals("武侯祠大街", road.get(0).getRoad());
    }

    @Test
    void statusText_mapsCodesToText() {
        assertEquals("畅通", client.statusText("1"));
        assertEquals("缓慢", client.statusText("2"));
        assertEquals("拥堵", client.statusText("3"));
        assertEquals("严重拥堵", client.statusText("4"));
        assertEquals("未知", client.statusText("9"));
    }
}

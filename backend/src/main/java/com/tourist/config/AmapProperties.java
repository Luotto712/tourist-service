package com.tourist.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图 Web 服务配置（application.yml 的 amap.*）。
 * 数据口径：成都市武侯区。
 */
@Component
@ConfigurationProperties(prefix = "amap")
public class AmapProperties {

    /** 高德 Web 服务 key；为空时 AmapClient 走 mock 降级 */
    private String key;

    /** 请求超时（毫秒） */
    private int timeoutMs = 5000;

    /** 缓存分钟数，降低配额 */
    private int cacheMinutes = 10;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getCacheMinutes() {
        return cacheMinutes;
    }

    public void setCacheMinutes(int cacheMinutes) {
        this.cacheMinutes = cacheMinutes;
    }
}

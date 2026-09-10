package com.tourist.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple rate limiting filter using in-memory token bucket.
 * Limits requests to sensitive endpoints (login, forgot-password).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    // Max attempts per window
    private static final int MAX_ATTEMPTS = 10;
    // Window duration in milliseconds (1 minute)
    private static final long WINDOW_MS = 60_000;

    private final Map<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Only rate-limit auth endpoints
        if (("/api/auth/login".equals(path) || "/api/auth/forgot-password".equals(path) || "/api/auth/reset-password".equals(path))
                && "POST".equalsIgnoreCase(method)) {
            String clientIp = getClientIp(request);
            String key = path + ":" + clientIp;

            RateLimitEntry entry = attempts.computeIfAbsent(key, k -> new RateLimitEntry());
            synchronized (entry) {
                long now = System.currentTimeMillis();
                if (now - entry.windowStart > WINDOW_MS) {
                    // Reset window
                    entry.windowStart = now;
                    entry.count = 0;
                }
                entry.count++;

                if (entry.count > MAX_ATTEMPTS) {
                    log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
                    response.setStatus(429);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !("/api/auth/login".equals(path) || "/api/auth/forgot-password".equals(path) || "/api/auth/reset-password".equals(path));
    }

    private static class RateLimitEntry {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }
}

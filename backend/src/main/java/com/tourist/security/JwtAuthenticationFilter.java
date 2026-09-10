package com.tourist.security;

import com.tourist.config.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Paths that do not require authentication.
     */
    private static final List<String> PERMIT_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/register"
    );

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Skip authentication for OPTIONS (preflight) requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip authentication for file download paths
        if (requestURI.startsWith("/api/files/download/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip authentication for banner GET requests
        if ("GET".equalsIgnoreCase(method) && requestURI.equals("/api/banners")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip authentication for permitted paths
        if (isPermitPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip if not an API request
        if (!requestURI.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Claims claims = jwtTokenProvider.parseToken(token);

                Long userId = claims.get("userId", Long.class);
                String username = claims.get("username", String.class);
                String role = claims.get("role", String.class);
                String college = claims.get("college", String.class);

                UserDetailsImpl userDetails = new UserDetailsImpl(
                        userId, username, null, role, college
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract Bearer token from Authorization header.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Check if the request path is in the permit list.
     */
    private boolean isPermitPath(String requestURI) {
        for (String path : PERMIT_PATHS) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

}

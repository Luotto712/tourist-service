package com.tourist.security;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.config.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserResolver.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(CurrentUserInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        // First try to get current user from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            CurrentUserInfo userInfo = new CurrentUserInfo();
            userInfo.setUserId(userDetails.getUserId());
            userInfo.setUsername(userDetails.getUsername());
            userInfo.setRole(userDetails.getRole());
            userInfo.setCollege(userDetails.getCollege());
            return userInfo;
        }

        // Fallback: extract from JWT token in request header
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request != null) {
            String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                String token = bearerToken.substring(BEARER_PREFIX.length());
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        Long userId = jwtTokenProvider.extractUserId(token);
                        String username = jwtTokenProvider.extractUsername(token);
                        String role = jwtTokenProvider.extractRole(token);
                        String college = jwtTokenProvider.extractCollege(token);

                        CurrentUserInfo userInfo = new CurrentUserInfo();
                        userInfo.setUserId(userId);
                        userInfo.setUsername(username);
                        userInfo.setRole(role);
                        userInfo.setCollege(college);
                        return userInfo;
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract user info from token: {}", e.getMessage());
                }
            }
        }

        // Return empty user info if no user found
        return new CurrentUserInfo();
    }

}

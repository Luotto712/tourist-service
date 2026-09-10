package com.tourist.aspect;

import com.tourist.annotation.RequireRole;
import com.tourist.common.BusinessException;
import com.tourist.common.ResultCode;
import com.tourist.security.UserDetailsImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class RoleCheckAspect {

    private static final Logger log = LoggerFactory.getLogger(RoleCheckAspect.class);

    @Around("@annotation(com.tourist.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get the @RequireRole annotation
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        String[] allowedRoles = requireRole.value();

        // Get current user from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            log.warn("No authenticated user found for method: {}", method.getName());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String currentUserRole = userDetails.getRole();

        // Check if the user's role is in the allowed roles
        boolean hasRole = false;
        for (String role : allowedRoles) {
            if (role.equals(currentUserRole)) {
                hasRole = true;
                break;
            }
        }

        if (!hasRole) {
            log.warn("User '{}' with role '{}' attempted to access method '{}' requiring roles: {}",
                    userDetails.getUsername(), currentUserRole, method.getName(), Arrays.toString(allowedRoles));
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "权限不足，当前角色: " + currentUserRole + "，需要角色: " + Arrays.toString(allowedRoles));
        }

        // Role check passed, proceed with the method
        return joinPoint.proceed();
    }

}

package com.tourist.aspect;

import com.tourist.annotation.CurrentUserInfo;
import com.tourist.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Around("@annotation(com.tourist.annotation.LogOperation)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        var annotation = ((MethodSignature) joinPoint.getSignature()).getMethod()
                .getAnnotation(com.tourist.annotation.LogOperation.class);

        Object result = joinPoint.proceed();

        try {
            String module = annotation.module();
            String action = annotation.action();

            Long userId = null;
            String username = "unknown";
            Object[] args = joinPoint.getArgs();
            Parameter[] params = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();
            for (int i = 0; i < params.length; i++) {
                if (params[i].isAnnotationPresent(com.tourist.annotation.CurrentUser.class) && args[i] instanceof CurrentUserInfo) {
                    CurrentUserInfo user = (CurrentUserInfo) args[i];
                    userId = user.getUserId();
                    username = user.getUsername();
                }
            }

            String ip = "127.0.0.1";
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                ip = request.getRemoteAddr();
            } catch (Exception ignored) {}

            operationLogService.log(module, action, null, annotation.detail(), userId, username, ip);
        } catch (Exception e) {
            // Logging failure should never break business
        }

        return result;
    }
}

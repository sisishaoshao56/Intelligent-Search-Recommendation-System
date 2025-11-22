package com.csu.demo.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    public AuthInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token != null && !token.isBlank()) {
            String userIdStr = redisTemplate.opsForValue().get("auth:token:" + token);
            if (userIdStr != null && !userIdStr.isBlank()) {
                try {
                    UserContext.setUserId(Integer.valueOf(userIdStr));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && !header.isBlank()) {
            return header.startsWith("Bearer ") ? header.substring(7).trim() : header.trim();
        }
        String param = request.getParameter("token");
        if (param != null && !param.isBlank()) {
            return param.trim();
        }
        return null;
    }
}

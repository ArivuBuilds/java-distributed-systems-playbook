package com.arivubuilds.api.versioning.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DeprecationHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            DeprecatedEndpoint annotation = handlerMethod.getMethodAnnotation(DeprecatedEndpoint.class);
            if (annotation != null) {
                response.setHeader("Deprecation", "true");
                response.setHeader("Sunset", annotation.sunsetDate());
                response.setHeader("Link", "<" + annotation.successorUri() + ">; rel=\"successor-version\"");
            }
        }
        return true;
    }
}
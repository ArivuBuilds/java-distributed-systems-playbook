package com.arivubuilds.api.versioning.config;

import com.arivubuilds.api.versioning.interceptor.DeprecationHeaderInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebVersioningConfig implements WebMvcConfigurer {

    private final DeprecationHeaderInterceptor deprecationInterceptor;

    public WebVersioningConfig(DeprecationHeaderInterceptor deprecationInterceptor) {
        this.deprecationInterceptor = deprecationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(deprecationInterceptor);
    }
}
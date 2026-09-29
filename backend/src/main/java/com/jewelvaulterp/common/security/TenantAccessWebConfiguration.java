package com.jewelvaulterp.common.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TenantAccessWebConfiguration implements WebMvcConfigurer {

    private final TenantAccessInterceptor tenantAccessInterceptor;

    public TenantAccessWebConfiguration(TenantAccessInterceptor tenantAccessInterceptor) {
        this.tenantAccessInterceptor = tenantAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantAccessInterceptor).addPathPatterns("/api/**");
    }
}
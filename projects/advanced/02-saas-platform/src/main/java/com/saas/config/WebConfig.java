package com.saas.config;

import com.saas.interceptor.RateLimitInterceptor;
import com.saas.interceptor.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final TenantInterceptor tenantInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(TenantInterceptor tenantInterceptor, RateLimitInterceptor rateLimitInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor);
        registry.addInterceptor(rateLimitInterceptor);
    }
}

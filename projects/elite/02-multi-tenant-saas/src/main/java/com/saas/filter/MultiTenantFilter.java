package com.saas.filter;

import com.saas.tenant.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Order(1)
public class MultiTenantFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(MultiTenantFilter.class);

    @Value("${tenant.header-name:X-Tenant-Id}")
    private String tenantHeaderName;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = httpRequest.getHeader(tenantHeaderName);
        if (tenantId != null && !tenantId.isBlank()) {
            TenantContext.setTenantId(tenantId);
            log.debug("Set tenant context to: {}", tenantId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

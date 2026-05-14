package com.k8sdemo.config;

import com.k8sdemo.service.GracefulShutdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ActuatorConfig {

    private final GracefulShutdownService shutdownService;

    @Bean
    public HealthIndicator readinessHealthIndicator() {
        return () -> {
            if (shutdownService.isShuttingDown()) {
                return Health.down()
                    .withDetail("reason", "shutting down")
                    .build();
            }
            return Health.up().build();
        };
    }

    @Bean
    public HealthIndicator livenessHealthIndicator() {
        return () -> Health.up().build();
    }
}

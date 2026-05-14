package com.saas.config;

import com.saas.tenant.TenantConnectionProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
public class MultiTenantDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource() {
        var defaultDataSource = dataSourceProperties().initializeDataSourceBuilder().build();

        var routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TenantConnectionProvider.getCurrentTenant();
            }
        };

        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        routingDataSource.setTargetDataSources(new HashMap<>());
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }
}

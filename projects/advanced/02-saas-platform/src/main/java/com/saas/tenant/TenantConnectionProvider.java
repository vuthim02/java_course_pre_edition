package com.saas.tenant;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantConnectionProvider {
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    public static String getCurrentTenant() {
        var tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : "default";
    }

    public void addTenant(String tenantId, DataSource dataSource) {
        dataSourceMap.put(tenantId, dataSource);
    }

    public void removeTenant(String tenantId) {
        dataSourceMap.remove(tenantId);
    }

    public DataSource getDataSource(String tenantId) {
        return dataSourceMap.get(tenantId);
    }

    public Connection getConnection(String tenantId) throws SQLException {
        var ds = dataSourceMap.get(tenantId);
        if (ds == null) {
            throw new SQLException("No datasource for tenant: " + tenantId);
        }
        return ds.getConnection();
    }
}

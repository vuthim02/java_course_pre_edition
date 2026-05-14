package com.saas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_records")
public class UsageRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String metric;

    @Column(nullable = false)
    private long value;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }
    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}

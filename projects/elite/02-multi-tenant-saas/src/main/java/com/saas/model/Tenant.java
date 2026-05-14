package com.saas.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "public")
public class Tenant {

    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String slug;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionTier tier;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TenantStatus status;
    private String schemaName;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant activatedAt;
    private Instant suspendedAt;
    @Column(nullable = false)
    private int maxUsers;
    @Column(nullable = false)
    private int maxStorageGb;
    @Column(nullable = false)
    private boolean featuresEnabled;

    @PrePersist
    void onCreate() {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        status = TenantStatus.PENDING;
        schemaName = "tenant_" + slug.replace("-", "_");
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SubscriptionTier getTier() { return tier; }
    public void setTier(SubscriptionTier tier) { this.tier = tier; }
    public TenantStatus getStatus() { return status; }
    public void setStatus(TenantStatus status) { this.status = status; }
    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
    public Instant getCreatedAt() { return createdAt; }
    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
    public int getMaxStorageGb() { return maxStorageGb; }
    public void setMaxStorageGb(int maxStorageGb) { this.maxStorageGb = maxStorageGb; }
    public void setActivatedAt(Instant activatedAt) { this.activatedAt = activatedAt; }
    public void setSuspendedAt(Instant suspendedAt) { this.suspendedAt = suspendedAt; }
}



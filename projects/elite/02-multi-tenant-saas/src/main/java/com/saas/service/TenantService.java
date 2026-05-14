package com.saas.service;

import com.saas.model.Tenant;
import com.saas.repository.TenantRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant createTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenant(UUID id) {
        return tenantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tenant not found: " + id));
    }

    public Tenant getTenantBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Tenant not found: " + slug));
    }

    public Tenant updateTenant(UUID id, Tenant updated) {
        Tenant tenant = getTenant(id);
        tenant.setName(updated.getName());
        tenant.setTier(updated.getTier());
        tenant.setMaxUsers(updated.getMaxUsers());
        tenant.setMaxStorageGb(updated.getMaxStorageGb());
        return tenantRepository.save(tenant);
    }

    public void deleteTenant(UUID id) {
        tenantRepository.deleteById(id);
    }

    public Tenant activateTenant(UUID id) {
        Tenant tenant = getTenant(id);
        tenant.setStatus(com.saas.model.TenantStatus.ACTIVE);
        tenant.setActivatedAt(java.time.Instant.now());
        return tenantRepository.save(tenant);
    }

    public Tenant suspendTenant(UUID id) {
        Tenant tenant = getTenant(id);
        tenant.setStatus(com.saas.model.TenantStatus.SUSPENDED);
        tenant.setSuspendedAt(java.time.Instant.now());
        return tenantRepository.save(tenant);
    }
}

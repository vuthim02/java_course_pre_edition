package com.saas.service;

import com.saas.dto.CreateTenantRequest;
import com.saas.dto.TenantDTO;
import com.saas.model.SubscriptionPlan;
import com.saas.model.Tenant;
import com.saas.model.TenantStatus;
import com.saas.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {
    private final TenantRepository repository;

    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    public List<TenantDTO> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public TenantDTO findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
    }

    public TenantDTO create(CreateTenantRequest request) {
        if (repository.existsByTenantId(request.tenantId())) {
            throw new RuntimeException("Tenant already exists: " + request.tenantId());
        }
        var tenant = new Tenant();
        tenant.setTenantId(request.tenantId());
        tenant.setName(request.name());
        tenant.setPlan(request.plan());
        tenant.setStatus(TenantStatus.ACTIVE);
        return toDto(repository.save(tenant));
    }

    public TenantDTO activate(String tenantId) {
        return updateStatus(tenantId, TenantStatus.ACTIVE);
    }

    public TenantDTO suspend(String tenantId) {
        return updateStatus(tenantId, TenantStatus.SUSPENDED);
    }

    public TenantDTO cancel(String tenantId) {
        return updateStatus(tenantId, TenantStatus.CANCELLED);
    }

    public TenantDTO changePlan(String tenantId, SubscriptionPlan newPlan) {
        var tenant = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        tenant.setPlan(newPlan);
        return toDto(repository.save(tenant));
    }

    private TenantDTO updateStatus(String tenantId, TenantStatus status) {
        var tenant = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        tenant.setStatus(status);
        return toDto(repository.save(tenant));
    }

    private TenantDTO toDto(Tenant tenant) {
        return new TenantDTO(tenant.getId(), tenant.getTenantId(), tenant.getName(),
                tenant.getPlan(), tenant.getStatus(), tenant.getCreatedAt(), tenant.getUpdatedAt());
    }
}

package com.saas.controller;

import com.saas.dto.CreateTenantRequest;
import com.saas.dto.TenantDTO;
import com.saas.model.SubscriptionPlan;
import com.saas.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantService service;

    public TenantController(TenantService service) {
        this.service = service;
    }

    @GetMapping
    public List<TenantDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{tenantId}")
    public TenantDTO getByTenantId(@PathVariable String tenantId) {
        return service.findByTenantId(tenantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDTO create(@Valid @RequestBody CreateTenantRequest request) {
        return service.create(request);
    }

    @PostMapping("/{tenantId}/activate")
    public TenantDTO activate(@PathVariable String tenantId) {
        return service.activate(tenantId);
    }

    @PostMapping("/{tenantId}/suspend")
    public TenantDTO suspend(@PathVariable String tenantId) {
        return service.suspend(tenantId);
    }

    @PostMapping("/{tenantId}/cancel")
    public TenantDTO cancel(@PathVariable String tenantId) {
        return service.cancel(tenantId);
    }

    @PutMapping("/{tenantId}/plan")
    public TenantDTO changePlan(@PathVariable String tenantId, @RequestParam SubscriptionPlan plan) {
        return service.changePlan(tenantId, plan);
    }
}

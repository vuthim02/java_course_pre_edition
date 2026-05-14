package com.saas.controller;

import com.saas.model.Tenant;
import com.saas.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant) {
        Tenant created = tenantService.createTenant(tenant);
        return ResponseEntity.created(URI.create("/api/tenants/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenant(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getTenant(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable UUID id, @RequestBody Tenant tenant) {
        return ResponseEntity.ok(tenantService.updateTenant(id, tenant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Tenant> activateTenant(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.activateTenant(id));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Tenant> suspendTenant(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.suspendTenant(id));
    }
}

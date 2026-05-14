# ADR-007: Multi-Tenancy Strategy

## Status

Accepted

## Context

SaaS-focused modules (Advanced SaaS Platform, Elite Multi-Tenant SaaS) require multi-tenant data isolation. Three common strategies exist: database-per-tenant, schema-per-tenant, and shared schema with tenant discriminator column.

## Decision

Use **database-per-tenant** as the primary strategy with **discriminator-column** as a fallback for simpler modules.

**Database-per-tenant:**
- Each tenant gets a dedicated PostgreSQL database
- Connection routing via `TenantContext` (ThreadLocal) + routing datasource
- Tenant databases created on registration via Flyway migration
- Used in: Elite multi-tenant SaaS module

**Discriminator Column:**
- Single database with `tenant_id` column on every table
- `@Where` annotations or Spring Data's `@TenantId` filtering
- Used in: SaaS platform module, shared infrastructure

Rationale for database-per-tenant:
- Strongest isolation — no risk of cross-tenant data leaks
- Per-tenant backup, restore, and point-in-time recovery
- Simplified compliance (GDPR data deletion per tenant)
- No application-level filtering code needed in queries

## Consequences

**Pros:**
- Maximum data isolation and security
- Per-tenant performance tuning and resource allocation
- Tenant migration/separation is straightforward
- Natural fit for enterprise compliance requirements

**Cons:**
- Higher resource usage (more database connections)
- Connection pooling complexity per tenant
- Schema changes must be applied across all tenant databases
- Infrastructure overhead for database provisioning

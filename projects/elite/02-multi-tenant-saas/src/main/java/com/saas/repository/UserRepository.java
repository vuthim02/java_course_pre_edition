package com.saas.repository;

import com.saas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByTenantId(UUID tenantId);
    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);
}

package com.k8sdemo.repository;

import com.k8sdemo.model.LeaderLease;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LeaderLeaseRepository extends JpaRepository<LeaderLease, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LeaderLease l WHERE l.id = 1")
    Optional<LeaderLease> findLeaseForUpdate();

    @Query("SELECT l FROM LeaderLease l WHERE l.expiresAt > ?1")
    Optional<LeaderLease> findActiveLease(LocalDateTime now);

    Optional<LeaderLease> findByInstanceId(String instanceId);
}

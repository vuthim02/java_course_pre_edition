package com.saas.repository;

import com.saas.model.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {
    List<UsageRecord> findByTenantIdAndMetricAndRecordedAtBetween(
            String tenantId, String metric, LocalDateTime start, LocalDateTime end);

    List<UsageRecord> findByTenantId(String tenantId);
}

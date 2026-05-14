package com.saas.service;

import com.saas.dto.UsageRecordDTO;
import com.saas.model.UsageRecord;
import com.saas.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsageTrackingService {
    private final UsageRecordRepository repository;

    public UsageTrackingService(UsageRecordRepository repository) {
        this.repository = repository;
    }

    public UsageRecordDTO recordUsage(String tenantId, String metric, long value) {
        var record = new UsageRecord();
        record.setTenantId(tenantId);
        record.setMetric(metric);
        record.setValue(value);
        return toDto(repository.save(record));
    }

    public List<UsageRecordDTO> getUsage(String tenantId, String metric,
                                          LocalDateTime start, LocalDateTime end) {
        return repository.findByTenantIdAndMetricAndRecordedAtBetween(tenantId, metric, start, end)
                .stream().map(this::toDto).toList();
    }

    public long getTotalUsage(String tenantId, String metric, LocalDateTime start, LocalDateTime end) {
        return repository.findByTenantIdAndMetricAndRecordedAtBetween(tenantId, metric, start, end)
                .stream().mapToLong(UsageRecord::getValue).sum();
    }

    private UsageRecordDTO toDto(UsageRecord record) {
        return new UsageRecordDTO(record.getId(), record.getTenantId(), record.getMetric(),
                record.getValue(), record.getRecordedAt());
    }
}

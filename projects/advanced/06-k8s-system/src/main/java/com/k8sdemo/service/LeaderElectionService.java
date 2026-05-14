package com.k8sdemo.service;

import com.k8sdemo.model.LeaderLease;
import com.k8sdemo.repository.LeaderLeaseRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class LeaderElectionService {

    private final LeaderLeaseRepository repository;

    @Getter
    private volatile boolean leader = false;

    private final String instanceId;

    public LeaderElectionService(LeaderLeaseRepository repository) {
        this.repository = repository;
        var hostname = System.getenv("HOSTNAME");
        this.instanceId = (hostname != null && !hostname.isBlank())
            ? hostname
            : "instance-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    @Scheduled(fixedRateString = "${leader.election.interval:10000}")
    public void attemptLeadership() {
        try {
            var existing = repository.findLeaseForUpdate();
            var now = LocalDateTime.now();

            if (existing.isEmpty()) {
                repository.save(LeaderLease.builder()
                    .id(1L)
                    .instanceId(instanceId)
                    .acquiredAt(now)
                    .expiresAt(now.plusSeconds(30))
                    .build());
                leader = true;
                log.info("Acquired leadership as {}", instanceId);
            } else {
                var lease = existing.get();
                if (lease.getInstanceId().equals(instanceId)) {
                    lease.setExpiresAt(now.plusSeconds(30));
                    repository.save(lease);
                    leader = true;
                    log.debug("Renewed leadership lease");
                } else if (lease.getExpiresAt().isBefore(now)) {
                    lease.setInstanceId(instanceId);
                    lease.setAcquiredAt(now);
                    lease.setExpiresAt(now.plusSeconds(30));
                    repository.save(lease);
                    leader = true;
                    log.info("Took over expired leadership from previous holder");
                } else {
                    leader = false;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to acquire leadership: {}", e.getMessage());
            leader = false;
        }
    }

    public Optional<LeaderLease> getCurrentLeader() {
        return repository.findActiveLease(LocalDateTime.now());
    }

    public String getInstanceId() {
        return instanceId;
    }
}

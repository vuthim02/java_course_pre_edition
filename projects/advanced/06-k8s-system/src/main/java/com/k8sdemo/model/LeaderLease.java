package com.k8sdemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leader_leases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderLease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String instanceId;

    @Column(nullable = false)
    private LocalDateTime acquiredAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}

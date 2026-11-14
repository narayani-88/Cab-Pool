package com.cabpool.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Transactional Outbox Pattern Entity:
 * Solves the dual-write "Save-then-publish" gap.
 * Both the entity change and this outbox event are saved within the same DB transaction.
 * A background worker or CDC tool (e.g. Debezium) publishes it to Kafka, guaranteeing At-Least-Once delivery.
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 50)
    private String aggregateId;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 15)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SENT, FAILED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

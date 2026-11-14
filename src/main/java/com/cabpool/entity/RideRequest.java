package com.cabpool.entity;

import com.cabpool.common.enums.Direction;
import com.cabpool.common.enums.RideRequestPreference;
import com.cabpool.common.enums.RideRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ride_requests", indexes = {
        @Index(name = "idx_requests_open", columnList = "venue, direction, status, target_time"),
        @Index(name = "idx_requests_zone", columnList = "drop_zone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String venue; // e.g. KIA_AIRPORT, CHINNASWAMY_STADIUM, MANYATA_TECH_PARK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private Direction direction = Direction.FROM_VENUE;

    @Column(name = "event_identifier", length = 60)
    private String eventIdentifier; // e.g. Flight 6E-204, Coldplay Concert, IPL CSK vs RCB

    @Column(name = "target_time", nullable = false)
    private OffsetDateTime targetTime; // Arrival/Departure/Show end time

    @Column(name = "max_wait_minutes", nullable = false)
    @Builder.Default
    private Integer maxWaitMinutes = 30;

    @Column(name = "drop_address", nullable = false, columnDefinition = "TEXT")
    private String dropAddress;

    @Column(name = "drop_lat", nullable = false)
    private Double dropLat;

    @Column(name = "drop_lng", nullable = false)
    private Double dropLng;

    @Column(name = "drop_zone", nullable = false, length = 40)
    private String dropZone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    @Builder.Default
    private RideRequestPreference preference = RideRequestPreference.MIXED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    @Builder.Default
    private RideRequestStatus status = RideRequestStatus.OPEN;

    /**
     * Optimistic locking (@Version).
     * Stops two concurrent UPDATE transactions from overwriting each other's changes.
     * Note: This prevents dirty UPDATEs; concurrent INSERTs are prevented by the UNIQUE constraint.
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

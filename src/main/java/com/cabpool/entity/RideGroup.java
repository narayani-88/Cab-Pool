package com.cabpool.entity;

import com.cabpool.common.enums.Direction;
import com.cabpool.common.enums.RideGroupStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ride_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private Direction direction = Direction.FROM_VENUE;

    @Column(name = "women_only", nullable = false)
    @Builder.Default
    private boolean womenOnly = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    @Builder.Default
    private RideGroupStatus status = RideGroupStatus.PROPOSED;

    @Column(name = "planned_pickup_time")
    private OffsetDateTime plannedPickupTime;

    @Column(name = "est_total_fare", precision = 10, scale = 2)
    private BigDecimal estTotalFare;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "rideGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<RideGroupMember> members = new ArrayList<>();
}

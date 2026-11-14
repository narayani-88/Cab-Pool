package com.cabpool.event;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RideConfirmedEvent implements Serializable {
    private UUID groupId;
    private String venue;
    private OffsetDateTime plannedPickupTime;
    private BigDecimal totalFare;
    private List<UUID> confirmedRequestIds;
    private List<UUID> confirmedUserIds;
}

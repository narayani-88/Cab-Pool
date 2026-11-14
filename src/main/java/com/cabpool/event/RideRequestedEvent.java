package com.cabpool.event;

import com.cabpool.common.enums.Direction;
import com.cabpool.common.enums.RideRequestPreference;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RideRequestedEvent implements Serializable {
    private UUID requestId;
    private UUID userId;
    private String venue;
    private Direction direction;
    private String eventIdentifier;
    private OffsetDateTime targetTime;
    private int maxWaitMinutes;
    private String dropAddress;
    private double dropLat;
    private double dropLng;
    private String dropZone;
    private RideRequestPreference preference;
}

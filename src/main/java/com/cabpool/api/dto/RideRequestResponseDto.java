package com.cabpool.api.dto;

import com.cabpool.common.enums.Direction;
import com.cabpool.common.enums.RideRequestPreference;
import com.cabpool.common.enums.RideRequestStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestResponseDto {
    private UUID id;
    private UUID userId;
    private String venue;
    private Direction direction;
    private String eventIdentifier;
    private OffsetDateTime targetTime;
    private Integer maxWaitMinutes;
    private String dropAddress;
    private Double dropLat;
    private Double dropLng;
    private String dropZone;
    private RideRequestPreference preference;
    private RideRequestStatus status;
    private Long version;
    private OffsetDateTime createdAt;
    private Long measuredIngestionTimeMs;
}

package com.cabpool.api.dto;

import com.cabpool.common.enums.Direction;
import com.cabpool.common.enums.RideRequestPreference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRideRequestDto {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotBlank(message = "venue is required (e.g. KIA_AIRPORT, CHINNASWAMY_STADIUM, MANYATA_TECH_PARK)")
    private String venue;

    @Builder.Default
    private Direction direction = Direction.FROM_VENUE;

    private String eventIdentifier; // e.g. "Flight 6E-204", "Coldplay Music of the Spheres", "IPL RCB vs CSK"

    @NotNull(message = "targetTime is required")
    private OffsetDateTime targetTime;

    @Builder.Default
    private Integer maxWaitMinutes = 30;

    @NotBlank(message = "dropAddress is required")
    private String dropAddress;

    @NotNull(message = "dropLat is required")
    private Double dropLat;

    @NotNull(message = "dropLng is required")
    private Double dropLng;

    @NotBlank(message = "dropZone is required (e.g. HSR, WHITEFIELD, INDIRANAGAR)")
    private String dropZone;

    @Builder.Default
    private RideRequestPreference preference = RideRequestPreference.MIXED;
}

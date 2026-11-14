package com.cabpool.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDelayDto {

    @NotBlank(message = "eventIdentifier is required (e.g. Flight 6E-204, Coldplay Tour, IPL CSK vs RCB)")
    private String eventIdentifier;

    private OffsetDateTime originalTargetTime;

    @Positive(message = "delayMinutes must be positive")
    private int delayMinutes;
}

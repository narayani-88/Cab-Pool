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
public class FlightDelayDto {

    @NotBlank(message = "flightNo is required (e.g. 6E-204)")
    private String flightNo;

    private OffsetDateTime originalArrivalTime;

    @Positive(message = "delayMinutes must be positive")
    private int delayMinutes;
}

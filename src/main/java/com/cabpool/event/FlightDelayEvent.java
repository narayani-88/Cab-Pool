package com.cabpool.event;

import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FlightDelayEvent implements Serializable {
    private String flightNo;
    private OffsetDateTime originalArrivalTime;
    private OffsetDateTime newArrivalTime;
    private int delayMinutes;
}

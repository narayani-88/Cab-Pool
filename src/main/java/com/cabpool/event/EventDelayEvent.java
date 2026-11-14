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
public class EventDelayEvent implements Serializable {
    private String eventIdentifier; // e.g. "Flight 6E-204", "Coldplay Tour", "IPL RCB vs CSK"
    private OffsetDateTime originalTargetTime;
    private OffsetDateTime newTargetTime;
    private int delayMinutes;
}

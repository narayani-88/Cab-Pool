package com.cabpool.flight.service;

import com.cabpool.event.EventDelayEvent;
import com.cabpool.flight.dto.EventDelayDto;
import com.cabpool.flight.dto.FlightDelayDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final EventTrackingService eventTrackingService;

    public EventDelayEvent broadcastFlightDelay(FlightDelayDto dto) {
        EventDelayDto eventDto = EventDelayDto.builder()
                .eventIdentifier(dto.getFlightNo().toUpperCase())
                .originalTargetTime(dto.getOriginalArrivalTime())
                .delayMinutes(dto.getDelayMinutes())
                .build();

        return eventTrackingService.broadcastEventDelay(eventDto);
    }
}

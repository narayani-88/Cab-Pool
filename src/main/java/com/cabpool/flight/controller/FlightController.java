package com.cabpool.flight.controller;

import com.cabpool.event.EventDelayEvent;
import com.cabpool.flight.dto.EventDelayDto;
import com.cabpool.flight.dto.FlightDelayDto;
import com.cabpool.flight.service.EventTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FlightController {

    private final EventTrackingService eventTrackingService;

    @PostMapping("/delay")
    public ResponseEntity<EventDelayEvent> simulateFlightDelay(@Valid @RequestBody FlightDelayDto dto) {
        EventDelayDto eventDto = EventDelayDto.builder()
                .eventIdentifier(dto.getFlightNo())
                .originalTargetTime(dto.getOriginalArrivalTime())
                .delayMinutes(dto.getDelayMinutes())
                .build();
        return ResponseEntity.ok(eventTrackingService.broadcastEventDelay(eventDto));
    }
}

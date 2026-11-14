package com.cabpool.flight.controller;

import com.cabpool.event.EventDelayEvent;
import com.cabpool.flight.dto.EventDelayDto;
import com.cabpool.flight.service.EventTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventTrackingController {

    private final EventTrackingService eventTrackingService;

    @PostMapping("/delay")
    public ResponseEntity<EventDelayEvent> broadcastDelay(@Valid @RequestBody EventDelayDto dto) {
        return ResponseEntity.ok(eventTrackingService.broadcastEventDelay(dto));
    }
}

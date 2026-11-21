package com.cabpool.simulation.controller;

import com.cabpool.api.dto.RideRequestResponseDto;
import com.cabpool.entity.User;
import com.cabpool.repository.UserRepository;
import com.cabpool.simulation.dto.SystemMetricsDto;
import com.cabpool.simulation.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;
    private final UserRepository userRepository;

    @PostMapping("/burst")
    public ResponseEntity<Map<String, Object>> triggerBurst(
            @RequestParam(defaultValue = "KIA_AIRPORT") String venue,
            @RequestParam(defaultValue = "Flight 6E-204 (Indigo)") String eventIdentifier,
            @RequestParam(defaultValue = "10") int count) {

        List<RideRequestResponseDto> results = simulationService.triggerArrivalBurst(venue, eventIdentifier, count);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Fired concurrent burst of " + results.size() + " riders into Kafka",
                "requestsGenerated", results.size(),
                "venue", venue,
                "event", eventIdentifier
        ));
    }

    @GetMapping("/metrics")
    public ResponseEntity<SystemMetricsDto> getMetrics() {
        return ResponseEntity.ok(simulationService.getSystemMetrics());
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetDemo() {
        simulationService.resetDemoData();
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "All requests, groups, and Redis pools reset."));
    }

    @GetMapping("/personas")
    public ResponseEntity<List<User>> getPersonas() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}

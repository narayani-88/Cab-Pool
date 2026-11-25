package com.cabpool.api.controller;

import com.cabpool.api.dto.CreateRideRequestDto;
import com.cabpool.api.dto.RideRequestResponseDto;
import com.cabpool.api.service.RideService;
import com.cabpool.entity.RideGroup;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
@CrossOrigin(origins = "*")
public class RideController {

    private final RideService rideService;
    private final com.cabpool.api.service.RiderNotificationService notificationService;

    public RideController(RideService rideService, com.cabpool.api.service.RiderNotificationService notificationService) {
        this.rideService = rideService;
        this.notificationService = notificationService;
    }

    @PostMapping("/requests")
    public ResponseEntity<RideRequestResponseDto> createRequest(@Valid @RequestBody CreateRideRequestDto dto) {
        RideRequestResponseDto response = rideService.createRideRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<RideRequestResponseDto> getRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(rideService.getRequestById(requestId));
    }

    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<List<RideRequestResponseDto>> getUserRequests(@PathVariable UUID userId) {
        return ResponseEntity.ok(rideService.getRequestsByUserId(userId));
    }

    @GetMapping("/users/{userId}/active")
    public ResponseEntity<RideRequestResponseDto> getActiveUserRequest(@PathVariable UUID userId) {
        return rideService.getActiveRequestForUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/groups/{groupId}/accept")
    public ResponseEntity<RideGroup> acceptGroup(
            @PathVariable UUID groupId,
            @RequestParam UUID requestId) {
        return ResponseEntity.ok(rideService.acceptGroupProposal(groupId, requestId));
    }

    @PostMapping("/groups/{groupId}/accept-all")
    public ResponseEntity<RideGroup> acceptAllMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(rideService.acceptAllGroupMembers(groupId));
    }

    @PostMapping("/requests/{requestId}/cancel")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable UUID requestId,
            @RequestParam(defaultValue = "Rider requested cancellation") String reason) {
        rideService.cancelRideRequest(requestId, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<RideGroup> getGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(rideService.getGroupById(groupId));
    }

    @PostMapping("/driver/groups/{groupId}/status")
    public ResponseEntity<java.util.Map<String, Object>> updateDriverStatus(
            @PathVariable UUID groupId,
            @RequestParam String status,
            @RequestParam(defaultValue = "Ramesh Kumar") String driverName,
            @RequestParam(defaultValue = "KA-01-MJ-4029") String vehicleNumber,
            @RequestParam(defaultValue = "Driver updated status") String message) {

        if ("ACCEPTED".equalsIgnoreCase(status)) {
            rideService.acceptAllGroupMembers(groupId);
        }
        notificationService.notifyDriverTripUpdate(groupId, status.toUpperCase(), message, driverName, vehicleNumber);
        return ResponseEntity.ok(java.util.Map.of(
                "status", "SUCCESS",
                "groupId", groupId,
                "driverStatus", status.toUpperCase(),
                "message", message
        ));
    }
}

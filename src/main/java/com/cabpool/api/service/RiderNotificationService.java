package com.cabpool.api.service;

import com.cabpool.event.EventDelayEvent;
import com.cabpool.event.GroupProposedEvent;
import com.cabpool.event.RideConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyRiderOfGroupProposal(UUID userId, GroupProposedEvent event) {
        String destination = "/topic/rider/" + userId;
        log.info("Sending WebSocket match notification to {}: Group {} proposed", destination, event.getGroupId());
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "GROUP_PROPOSED",
                "groupId", event.getGroupId(),
                "venue", event.getVenue(),
                "direction", event.getDirection(),
                "plannedPickupTime", event.getPlannedPickupTime(),
                "estTotalFare", event.getEstTotalFare(),
                "members", event.getMembers()
        ));
    }

    public void notifyRiderOfEventDelay(UUID userId, EventDelayEvent event) {
        String destination = "/topic/rider/" + userId;
        log.info("Sending event delay alert to {}: Event {} delayed by {} minutes",
                destination, event.getEventIdentifier(), event.getDelayMinutes());
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "EVENT_DELAY",
                "eventIdentifier", event.getEventIdentifier(),
                "delayMinutes", event.getDelayMinutes(),
                "newTargetTime", event.getNewTargetTime()
        ));
    }

    public void notifyRiderOfConfirmation(UUID userId, RideConfirmedEvent event) {
        String destination = "/topic/rider/" + userId;
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "RIDE_CONFIRMED",
                "groupId", event.getGroupId(),
                "venue", event.getVenue(),
                "plannedPickupTime", event.getPlannedPickupTime(),
                "totalFare", event.getTotalFare()
        ));
    }

    public void notifyRiderOfCancellation(UUID userId, String reason) {
        String destination = "/topic/rider/" + userId;
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "RIDE_CANCELLED",
                "reason", reason
        ));
    }

    public void notifyGroupStatusUpdate(UUID groupId, String status, String message) {
        String destination = "/topic/group/" + groupId;
        log.info("Broadcasting group status update to {}: {}", destination, status);
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "GROUP_STATUS_UPDATE",
                "groupId", groupId,
                "status", status,
                "message", message
        ));
    }
}

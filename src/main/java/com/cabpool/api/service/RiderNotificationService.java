package com.cabpool.api.service;

import com.cabpool.event.EventDelayEvent;
import com.cabpool.event.GroupProposedEvent;
import com.cabpool.event.RideConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class RiderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RiderNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public RiderNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

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

    public void notifyDriversOfNewDispatch(GroupProposedEvent event) {
        String destination = "/topic/drivers/dispatch";
        log.info("Broadcasting new pool dispatch alert to drivers: Group {}", event.getGroupId());
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "NEW_DISPATCH_OFFER",
                "groupId", event.getGroupId(),
                "venue", event.getVenue(),
                "direction", event.getDirection(),
                "plannedPickupTime", event.getPlannedPickupTime(),
                "estTotalFare", event.getEstTotalFare(),
                "passengerCount", event.getMembers().size(),
                "members", event.getMembers()
        ));
    }

    public void notifyDriverTripUpdate(UUID groupId, String status, String message, String driverName, String vehicleNumber) {
        String groupDest = "/topic/group/" + groupId;
        log.info("Broadcasting driver status to group {}: status={}, driver={}", groupId, status, driverName);
        messagingTemplate.convertAndSend(groupDest, Map.of(
                "type", "DRIVER_STATUS_UPDATE",
                "groupId", groupId,
                "status", status,
                "message", message,
                "driverName", driverName != null ? driverName : "Ramesh Kumar",
                "vehicleNumber", vehicleNumber != null ? vehicleNumber : "KA-01-MJ-4029"
        ));
    }
}

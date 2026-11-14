package com.cabpool.api.consumer;

import com.cabpool.api.service.RiderNotificationService;
import com.cabpool.common.enums.RideRequestStatus;
import com.cabpool.event.EventDelayEvent;
import com.cabpool.event.GroupProposedEvent;
import com.cabpool.event.RideCancelledEvent;
import com.cabpool.event.RideConfirmedEvent;
import com.cabpool.repository.RideRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiEventConsumer {

    private final RiderNotificationService notificationService;
    private final RideRequestRepository requestRepository;

    @KafkaListener(
            topics = "${cabpool.kafka.topics.ride-matched}",
            groupId = "cabpool-api-matched"
    )
    public void onGroupProposed(GroupProposedEvent event) {
        log.info("API Service consumed GroupProposedEvent for group: {}", event.getGroupId());
        for (GroupProposedEvent.MemberSummary member : event.getMembers()) {
            notificationService.notifyRiderOfGroupProposal(member.getUserId(), event);
        }
    }

    @KafkaListener(
            topics = "${cabpool.kafka.topics.ride-confirmed}",
            groupId = "cabpool-api-confirmed"
    )
    public void onRideConfirmed(RideConfirmedEvent event) {
        log.info("API Service consumed RideConfirmedEvent for group: {}", event.getGroupId());
        for (java.util.UUID userId : event.getConfirmedUserIds()) {
            notificationService.notifyRiderOfConfirmation(userId, event);
        }
    }

    @KafkaListener(
            topics = "${cabpool.kafka.topics.ride-cancelled}",
            groupId = "cabpool-api-cancelled"
    )
    public void onRideCancelled(RideCancelledEvent event) {
        log.info("API Service consumed RideCancelledEvent for request: {}", event.getCancelledRequestId());
        notificationService.notifyRiderOfCancellation(event.getCancelledUserId(), event.getReason());
    }

    @KafkaListener(
            topics = "${cabpool.kafka.topics.event-delayed}",
            groupId = "cabpool-api-delays"
    )
    public void onEventDelayed(EventDelayEvent event) {
        log.info("API Service consumed EventDelayEvent for event: {}", event.getEventIdentifier());
        requestRepository.findByEventIdentifierAndStatus(event.getEventIdentifier(), RideRequestStatus.OPEN)
                .ifPresent(req -> notificationService.notifyRiderOfEventDelay(req.getUserId(), event));
    }
}

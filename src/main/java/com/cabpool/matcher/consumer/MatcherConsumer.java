package com.cabpool.matcher.consumer;

import com.cabpool.common.enums.RideRequestStatus;
import com.cabpool.entity.RideRequest;
import com.cabpool.event.EventDelayEvent;
import com.cabpool.event.RideCancelledEvent;
import com.cabpool.event.RideRequestedEvent;
import com.cabpool.matcher.service.MatcherAlgorithmService;
import com.cabpool.repository.RideRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatcherConsumer {

    private final MatcherAlgorithmService matcherAlgorithmService;
    private final RideRequestRepository requestRepository;

    @KafkaListener(
            topics = "${cabpool.kafka.topics.ride-requested}",
            groupId = "cabpool-matcher-ride-requests"
    )
    public void onRideRequested(RideRequestedEvent event) {
        log.info("Matcher Service consumed RideRequestedEvent for request {}", event.getRequestId());
        matcherAlgorithmService.processNewRideRequest(event);
    }

    @KafkaListener(
            topics = "${cabpool.kafka.topics.ride-cancelled}",
            groupId = "cabpool-matcher-ride-cancelled"
    )
    public void onRideCancelled(RideCancelledEvent event) {
        log.info("Matcher Service consumed RideCancelledEvent for request: {}", event.getCancelledRequestId());
        if (event.getRemainingRequestIdsToRematch() != null && !event.getRemainingRequestIdsToRematch().isEmpty()) {
            matcherAlgorithmService.handleCancellationAndRematch(event);
        }
    }

    @KafkaListener(
            topics = "${cabpool.kafka.topics.event-delayed}",
            groupId = "cabpool-matcher-event-delayed"
    )
    public void onEventDelayed(EventDelayEvent event) {
        log.info("Matcher Service consumed EventDelayEvent for: {}", event.getEventIdentifier());

        requestRepository.findByEventIdentifierAndStatus(event.getEventIdentifier(), RideRequestStatus.OPEN)
                .ifPresent(request -> {
                    log.info("Updating target time for request {} from {} to {}",
                            request.getId(), request.getTargetTime(), event.getNewTargetTime());

                    request.setTargetTime(event.getNewTargetTime());
                    requestRepository.save(request);

                    RideRequestedEvent updatedEvent = RideRequestedEvent.builder()
                            .requestId(request.getId())
                            .userId(request.getUserId())
                            .venue(request.getVenue())
                            .direction(request.getDirection())
                            .eventIdentifier(request.getEventIdentifier())
                            .targetTime(request.getTargetTime())
                            .maxWaitMinutes(request.getMaxWaitMinutes())
                            .dropAddress(request.getDropAddress())
                            .dropLat(request.getDropLat())
                            .dropLng(request.getDropLng())
                            .dropZone(request.getDropZone())
                            .preference(request.getPreference())
                            .build();

                    matcherAlgorithmService.processNewRideRequest(updatedEvent);
                });
    }
}

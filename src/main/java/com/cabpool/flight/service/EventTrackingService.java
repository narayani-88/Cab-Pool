package com.cabpool.flight.service;

import com.cabpool.event.EventDelayEvent;
import com.cabpool.flight.dto.EventDelayDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventTrackingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${cabpool.kafka.topics.event-delayed}")
    private String eventDelayedTopic;

    /**
     * Broadcasts delay for any generalized event (Flight landing, Concert finish, Stadium match, Office shift).
     */
    public EventDelayEvent broadcastEventDelay(EventDelayDto dto) {
        OffsetDateTime original = dto.getOriginalTargetTime() != null
                ? dto.getOriginalTargetTime()
                : OffsetDateTime.now();

        OffsetDateTime delayed = original.plusMinutes(dto.getDelayMinutes());

        EventDelayEvent event = EventDelayEvent.builder()
                .eventIdentifier(dto.getEventIdentifier().trim())
                .originalTargetTime(original)
                .newTargetTime(delayed)
                .delayMinutes(dto.getDelayMinutes())
                .build();

        kafkaTemplate.send(eventDelayedTopic, event.getEventIdentifier(), event);
        log.info("EventTrackingService published delay for {} (+{} mins) to Kafka topic '{}'",
                event.getEventIdentifier(), event.getDelayMinutes(), eventDelayedTopic);

        return event;
    }
}

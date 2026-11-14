package com.cabpool.matcher.service;

import com.cabpool.common.enums.Gender;
import com.cabpool.common.enums.RideGroupStatus;
import com.cabpool.common.enums.RideRequestPreference;
import com.cabpool.common.enums.RideRequestStatus;
import com.cabpool.entity.RideGroup;
import com.cabpool.entity.RideGroupMember;
import com.cabpool.entity.RideGroupMemberId;
import com.cabpool.entity.RideRequest;
import com.cabpool.entity.User;
import com.cabpool.event.GroupProposedEvent;
import com.cabpool.event.RideCancelledEvent;
import com.cabpool.event.RideRequestedEvent;
import com.cabpool.repository.RideGroupMemberRepository;
import com.cabpool.repository.RideGroupRepository;
import com.cabpool.repository.RideRequestRepository;
import com.cabpool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatcherAlgorithmService {

    private final RedisPoolService redisPoolService;
    private final RideRequestRepository requestRepository;
    private final RideGroupRepository groupRepository;
    private final RideGroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${cabpool.kafka.topics.ride-matched}")
    private String rideMatchedTopic;

    @Value("${cabpool.matching.max-group-size:4}")
    private int maxGroupSize;

    @Value("${cabpool.matching.min-group-size:2}")
    private int minGroupSize;

    /**
     * Executes matching cycle with measured execution time.
     */
    public void processNewRideRequest(RideRequestedEvent incoming) {
        long startTimeMs = System.currentTimeMillis();
        log.info("Matcher Service processing candidate: {} [Venue: {}, Direction: {}, Zone: {}]",
                incoming.getRequestId(), incoming.getVenue(), incoming.getDirection(), incoming.getDropZone());

        // 1. Add to active in-memory Redis pool
        redisPoolService.addToActivePool(incoming);

        // 2. Query candidates from Redis
        List<RideRequestedEvent> candidates = redisPoolService.getCandidates(
                incoming.getVenue(), incoming.getDirection(), incoming.getDropZone());

        if (candidates.size() < minGroupSize) {
            long durationMs = System.currentTimeMillis() - startTimeMs;
            log.info("Pool has {}/{} required candidates for zone {}. Matching cycle completed in {} ms.",
                    candidates.size(), minGroupSize, incoming.getDropZone(), durationMs);
            return;
        }

        // 3. Cluster compatible candidates
        List<RideRequestedEvent> cluster = findCompatibleCluster(incoming, candidates);

        if (cluster.size() < minGroupSize) {
            long durationMs = System.currentTimeMillis() - startTimeMs;
            log.info("Cluster size ({}) below minimum group size ({}). Continuing to pool. Matching cycle took {} ms.",
                    cluster.size(), minGroupSize, durationMs);
            return;
        }

        if (cluster.size() > maxGroupSize) {
            cluster = cluster.subList(0, maxGroupSize);
        }

        // 4. Acquire atomic distributed locks in Redis on all candidate IDs
        List<UUID> requestIds = cluster.stream().map(RideRequestedEvent::getRequestId).toList();
        String lockOwner = UUID.randomUUID().toString();

        boolean locked = redisPoolService.acquireDistributedLocks(requestIds, lockOwner, 15);
        if (!locked) {
            long durationMs = System.currentTimeMillis() - startTimeMs;
            log.warn("Could not acquire locks for candidates {} in {} ms. Skipping cycle.", requestIds, durationMs);
            return;
        }

        try {
            // 5. Persist formed group in PostgreSQL
            GroupProposedEvent proposedEvent = createAndSaveGroup(cluster);

            // 6. Remove matched candidates from Redis pool
            redisPoolService.removeFromActivePool(incoming.getVenue(), incoming.getDirection(), incoming.getDropZone(), requestIds);

            // 7. Publish ride-matched event to Kafka
            kafkaTemplate.send(rideMatchedTopic, proposedEvent.getGroupId().toString(), proposedEvent);

            long totalDurationMs = System.currentTimeMillis() - startTimeMs;
            log.info("Matched {} riders into Group {}. Published to Kafka topic '{}'. Measured duration: {} ms",
                    cluster.size(), proposedEvent.getGroupId(), rideMatchedTopic, totalDurationMs);

        } catch (Exception ex) {
            log.error("Error creating group for candidates {}: {}", requestIds, ex.getMessage(), ex);
        } finally {
            // Safely release locks with Lua script
            redisPoolService.releaseDistributedLocks(requestIds, lockOwner);
        }
    }

    /**
     * Handles re-matching when a cancellation occurs.
     * Takes orphaned remaining riders, restores them to OPEN, and re-evaluates the pool.
     */
    @Transactional
    public void handleCancellationAndRematch(RideCancelledEvent event) {
        log.info("Re-matching triggered for {} remaining riders after cancellation of request {}",
                event.getRemainingRequestIdsToRematch().size(), event.getCancelledRequestId());

        for (UUID remainingId : event.getRemainingRequestIdsToRematch()) {
            requestRepository.findById(remainingId).ifPresent(req -> {
                req.setStatus(RideRequestStatus.OPEN);
                requestRepository.save(req);

                RideRequestedEvent rematchEvent = RideRequestedEvent.builder()
                        .requestId(req.getId())
                        .userId(req.getUserId())
                        .venue(req.getVenue())
                        .direction(req.getDirection())
                        .eventIdentifier(req.getEventIdentifier())
                        .targetTime(req.getTargetTime())
                        .maxWaitMinutes(req.getMaxWaitMinutes())
                        .dropAddress(req.getDropAddress())
                        .dropLat(req.getDropLat())
                        .dropLng(req.getDropLng())
                        .dropZone(req.getDropZone())
                        .preference(req.getPreference())
                        .build();

                // Re-evaluate in pool
                processNewRideRequest(rematchEvent);
            });
        }
    }

    private List<RideRequestedEvent> findCompatibleCluster(RideRequestedEvent primary, List<RideRequestedEvent> candidates) {
        List<RideRequestedEvent> matched = new ArrayList<>();
        matched.add(primary);

        boolean primaryWomenOnly = primary.getPreference() == RideRequestPreference.WOMEN_ONLY;

        for (RideRequestedEvent other : candidates) {
            if (other.getRequestId().equals(primary.getRequestId())) {
                continue;
            }

            // Time difference within wait tolerance
            long timeDiffMinutes = Math.abs(Duration.between(primary.getTargetTime(), other.getTargetTime()).toMinutes());
            if (timeDiffMinutes > primary.getMaxWaitMinutes() || timeDiffMinutes > other.getMaxWaitMinutes()) {
                continue;
            }

            // Gender / preference verification
            boolean otherWomenOnly = other.getPreference() == RideRequestPreference.WOMEN_ONLY;
            if (primaryWomenOnly || otherWomenOnly) {
                if (!isVerifiedFemale(primary.getUserId()) || !isVerifiedFemale(other.getUserId())) {
                    continue;
                }
            }

            matched.add(other);
            if (matched.size() == maxGroupSize) {
                break;
            }
        }

        return matched;
    }

    private boolean isVerifiedFemale(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> u.getGender() == Gender.FEMALE && u.isVerified())
                .orElse(false);
    }

    @Transactional
    public GroupProposedEvent createAndSaveGroup(List<RideRequestedEvent> cluster) {
        RideRequestedEvent first = cluster.get(0);
        boolean isWomenOnly = cluster.stream()
                .anyMatch(c -> c.getPreference() == RideRequestPreference.WOMEN_ONLY);

        cluster.sort(Comparator.comparingDouble(RideRequestedEvent::getDropLat));

        OffsetDateTime plannedPickup = cluster.stream()
                .map(RideRequestedEvent::getTargetTime)
                .max(OffsetDateTime::compareTo)
                .orElse(OffsetDateTime.now())
                .plusMinutes(15);

        BigDecimal totalFare = BigDecimal.valueOf(1200.00);
        BigDecimal sharePerMember = totalFare.divide(BigDecimal.valueOf(cluster.size()), 2, RoundingMode.HALF_UP);

        RideGroup group = RideGroup.builder()
                .venue(first.getVenue())
                .direction(first.getDirection())
                .womenOnly(isWomenOnly)
                .status(RideGroupStatus.PROPOSED)
                .plannedPickupTime(plannedPickup)
                .estTotalFare(totalFare)
                .build();

        RideGroup savedGroup = groupRepository.save(group);
        List<GroupProposedEvent.MemberSummary> memberSummaries = new ArrayList<>();

        for (int i = 0; i < cluster.size(); i++) {
            RideRequestedEvent rider = cluster.get(i);
            int dropOrder = i + 1;

            RideGroupMember member = RideGroupMember.builder()
                    .id(new RideGroupMemberId(savedGroup.getId(), rider.getRequestId()))
                    .rideGroup(savedGroup)
                    .requestId(rider.getRequestId())
                    .dropOrder(dropOrder)
                    .shareAmount(sharePerMember)
                    .accepted(false)
                    .build();

            memberRepository.save(member);

            requestRepository.findById(rider.getRequestId()).ifPresent(req -> {
                req.setStatus(RideRequestStatus.MATCHED);
                requestRepository.save(req);
            });

            memberSummaries.add(GroupProposedEvent.MemberSummary.builder()
                    .requestId(rider.getRequestId())
                    .userId(rider.getUserId())
                    .dropOrder(dropOrder)
                    .shareAmount(sharePerMember)
                    .dropZone(rider.getDropZone())
                    .dropAddress(rider.getDropAddress())
                    .build());
        }

        return GroupProposedEvent.builder()
                .groupId(savedGroup.getId())
                .venue(first.getVenue())
                .direction(first.getDirection())
                .womenOnly(isWomenOnly)
                .plannedPickupTime(plannedPickup)
                .estTotalFare(totalFare)
                .members(memberSummaries)
                .build();
    }
}

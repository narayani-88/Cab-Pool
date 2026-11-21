package com.cabpool.api.service;

import com.cabpool.api.dto.CreateRideRequestDto;
import com.cabpool.api.dto.RideRequestResponseDto;
import com.cabpool.common.enums.Gender;
import com.cabpool.common.enums.RideGroupStatus;
import com.cabpool.common.enums.RideRequestPreference;
import com.cabpool.common.enums.RideRequestStatus;
import com.cabpool.entity.RideGroup;
import com.cabpool.entity.RideGroupMember;
import com.cabpool.entity.RideGroupMemberId;
import com.cabpool.entity.RideRequest;
import com.cabpool.entity.User;
import com.cabpool.event.RideCancelledEvent;
import com.cabpool.event.RideConfirmedEvent;
import com.cabpool.event.RideRequestedEvent;
import com.cabpool.repository.RideGroupMemberRepository;
import com.cabpool.repository.RideGroupRepository;
import com.cabpool.repository.RideRequestRepository;
import com.cabpool.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RideGroupRepository groupRepository;
    private final RideGroupMemberRepository memberRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RiderNotificationService notificationService;

    public RideService(RideRequestRepository requestRepository,
                       UserRepository userRepository,
                       RideGroupRepository groupRepository,
                       RideGroupMemberRepository memberRepository,
                       KafkaTemplate<String, Object> kafkaTemplate,
                       RiderNotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
    }

    @Value("${cabpool.kafka.topics.ride-requested}")
    private String rideRequestedTopic;

    @Value("${cabpool.kafka.topics.ride-confirmed}")
    private String rideConfirmedTopic;

    @Value("${cabpool.kafka.topics.ride-cancelled}")
    private String rideCancelledTopic;

    @Transactional
    public RideRequestResponseDto createRideRequest(CreateRideRequestDto dto) {
        long startTime = System.currentTimeMillis();

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + dto.getUserId()));

        // Enforce Women-Only verification check
        if (dto.getPreference() == RideRequestPreference.WOMEN_ONLY) {
            if (user.getGender() != Gender.FEMALE || !user.isVerified()) {
                throw new IllegalStateException("Women-only ride pools require a verified female account.");
            }
        }

        // Clean up any old dangling OPEN requests for this user so they start a clean matching cycle
        List<RideRequest> userRequests = requestRepository.findByUserIdOrderByCreatedAtDesc(dto.getUserId());
        for (RideRequest existing : userRequests) {
            if (existing.getStatus() == RideRequestStatus.OPEN) {
                existing.setStatus(RideRequestStatus.CANCELLED);
                requestRepository.save(existing);
            }
        }

        RideRequest request = RideRequest.builder()
                .userId(dto.getUserId())
                .venue(dto.getVenue().toUpperCase())
                .direction(dto.getDirection())
                .eventIdentifier(dto.getEventIdentifier())
                .targetTime(dto.getTargetTime())
                .maxWaitMinutes(dto.getMaxWaitMinutes() != null ? dto.getMaxWaitMinutes() : 30)
                .dropAddress(dto.getDropAddress())
                .dropLat(dto.getDropLat())
                .dropLng(dto.getDropLng())
                .dropZone(dto.getDropZone().toUpperCase())
                .preference(dto.getPreference())
                .status(RideRequestStatus.OPEN)
                .build();

        RideRequest saved = requestRepository.save(request);

        // Emit RideRequestedEvent to Kafka
        RideRequestedEvent event = RideRequestedEvent.builder()
                .requestId(saved.getId())
                .userId(saved.getUserId())
                .venue(saved.getVenue())
                .direction(saved.getDirection())
                .eventIdentifier(saved.getEventIdentifier())
                .targetTime(saved.getTargetTime())
                .maxWaitMinutes(saved.getMaxWaitMinutes())
                .dropAddress(saved.getDropAddress())
                .dropLat(saved.getDropLat())
                .dropLng(saved.getDropLng())
                .dropZone(saved.getDropZone())
                .preference(saved.getPreference())
                .build();

        kafkaTemplate.send(rideRequestedTopic, saved.getVenue(), event);

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("Request {} ingested and published to Kafka '{}' in {} ms",
                saved.getId(), rideRequestedTopic, durationMs);

        RideRequestResponseDto response = mapToResponse(saved);
        response.setMeasuredIngestionTimeMs(durationMs);
        return response;
    }

    @Transactional
    public RideGroup acceptGroupProposal(UUID groupId, UUID requestId) {
        RideGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Ride group not found: " + groupId));

        RideGroupMemberId memberId = new RideGroupMemberId(groupId, requestId);
        RideGroupMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Rider request " + requestId + " is not a member of group " + groupId));

        member.setAccepted(true);
        memberRepository.save(member);
        log.info("Rider request {} accepted proposal for group {}", requestId, groupId);

        List<RideGroupMember> allMembers = memberRepository.findByRideGroupId(groupId);
        boolean allAccepted = allMembers.stream().allMatch(RideGroupMember::isAccepted);

        if (allAccepted) {
            group.setStatus(RideGroupStatus.CONFIRMED);
            groupRepository.save(group);

            List<UUID> confirmedRequestIds = new ArrayList<>();
            List<UUID> confirmedUserIds = new ArrayList<>();

            for (RideGroupMember m : allMembers) {
                requestRepository.findById(m.getRequestId()).ifPresent(req -> {
                    req.setStatus(RideRequestStatus.CONFIRMED);
                    requestRepository.save(req);
                    confirmedRequestIds.add(req.getId());
                    confirmedUserIds.add(req.getUserId());
                });
            }

            log.info("All members accepted! Group {} confirmed.", groupId);

            // Publish ride.confirmed event to Kafka
            RideConfirmedEvent confirmedEvent = RideConfirmedEvent.builder()
                    .groupId(groupId)
                    .venue(group.getVenue())
                    .plannedPickupTime(group.getPlannedPickupTime())
                    .totalFare(group.getEstTotalFare())
                    .confirmedRequestIds(confirmedRequestIds)
                    .confirmedUserIds(confirmedUserIds)
                    .build();

            kafkaTemplate.send(rideConfirmedTopic, groupId.toString(), confirmedEvent);
            log.info("Published RideConfirmedEvent to Kafka topic '{}'", rideConfirmedTopic);

            notificationService.notifyGroupStatusUpdate(groupId, "CONFIRMED", "All riders accepted! Cab is confirmed.");
        } else {
            notificationService.notifyGroupStatusUpdate(groupId, "WAITING_FOR_OTHERS", "A rider accepted. Waiting for others.");
        }

        return group;
    }

    @Transactional
    public RideGroup acceptAllGroupMembers(UUID groupId) {
        RideGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Ride group not found: " + groupId));

        List<RideGroupMember> allMembers = memberRepository.findByRideGroupId(groupId);
        for (RideGroupMember m : allMembers) {
            m.setAccepted(true);
            memberRepository.save(m);
        }

        group.setStatus(RideGroupStatus.CONFIRMED);
        groupRepository.save(group);

        List<UUID> confirmedRequestIds = new ArrayList<>();
        List<UUID> confirmedUserIds = new ArrayList<>();

        for (RideGroupMember m : allMembers) {
            requestRepository.findById(m.getRequestId()).ifPresent(req -> {
                req.setStatus(RideRequestStatus.CONFIRMED);
                requestRepository.save(req);
                confirmedRequestIds.add(req.getId());
                confirmedUserIds.add(req.getUserId());
            });
        }

        log.info("All members accepted for group {} via confirm-all", groupId);

        // Publish ride.confirmed event to Kafka
        RideConfirmedEvent confirmedEvent = RideConfirmedEvent.builder()
                .groupId(groupId)
                .venue(group.getVenue())
                .plannedPickupTime(group.getPlannedPickupTime())
                .totalFare(group.getEstTotalFare())
                .confirmedRequestIds(confirmedRequestIds)
                .confirmedUserIds(confirmedUserIds)
                .build();

        kafkaTemplate.send(rideConfirmedTopic, groupId.toString(), confirmedEvent);
        log.info("Published RideConfirmedEvent to Kafka topic '{}'", rideConfirmedTopic);

        notificationService.notifyGroupStatusUpdate(groupId, "CONFIRMED", "All riders accepted! Cab is confirmed.");
        return group;
    }

    /**
     * Cancels a ride request and triggers re-matching for any remaining group members.
     */
    @Transactional
    public void cancelRideRequest(UUID requestId, String reason) {
        RideRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Ride request not found: " + requestId));

        if (request.getStatus() == RideRequestStatus.CANCELLED) {
            return;
        }

        request.setStatus(RideRequestStatus.CANCELLED);
        requestRepository.save(request);

        List<UUID> remainingToRematch = new ArrayList<>();
        UUID previousGroupId = null;

        // Check if this request was in a proposed or confirmed group
        var memberOpt = memberRepository.findByRequestId(requestId);
        if (memberOpt.isPresent()) {
            RideGroupMember member = memberOpt.get();
            RideGroup group = member.getRideGroup();
            previousGroupId = group.getId();

            // Cancel the group
            group.setStatus(RideGroupStatus.CANCELLED);
            groupRepository.save(group);

            // Find other members who need re-matching
            List<RideGroupMember> siblings = memberRepository.findByRideGroupId(group.getId());
            for (RideGroupMember sib : siblings) {
                if (!sib.getRequestId().equals(requestId)) {
                    remainingToRematch.add(sib.getRequestId());
                }
            }

            // Clean up member record for cancelled rider
            memberRepository.delete(member);
        }

        // Emit ride.cancelled event to Kafka with remaining riders for re-matching
        RideCancelledEvent event = RideCancelledEvent.builder()
                .cancelledRequestId(requestId)
                .cancelledUserId(request.getUserId())
                .reason(reason)
                .previousGroupId(previousGroupId)
                .remainingRequestIdsToRematch(remainingToRematch)
                .build();

        kafkaTemplate.send(rideCancelledTopic, request.getVenue(), event);
        log.info("Published RideCancelledEvent to Kafka topic '{}' for request {}. Rematch count: {}",
                rideCancelledTopic, requestId, remainingToRematch.size());

        notificationService.notifyRiderOfCancellation(request.getUserId(), reason);
    }

    @Transactional(readOnly = true)
    public RideRequestResponseDto getRequestById(UUID id) {
        return requestRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Ride request not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<RideRequestResponseDto> getRequestsByUserId(UUID userId) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.Optional<RideRequestResponseDto> getActiveRequestForUser(UUID userId) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(r -> r.getStatus() == RideRequestStatus.OPEN || r.getStatus() == RideRequestStatus.MATCHED)
                .findFirst()
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public RideGroup getGroupById(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Ride group not found: " + groupId));
    }

    private RideRequestResponseDto mapToResponse(RideRequest request) {
        return RideRequestResponseDto.builder()
                .id(request.getId())
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
                .status(request.getStatus())
                .version(request.getVersion())
                .createdAt(request.getCreatedAt())
                .build();
    }
}

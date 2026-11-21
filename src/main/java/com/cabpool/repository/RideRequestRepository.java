package com.cabpool.repository;

import com.cabpool.common.enums.Direction;
import com.cabpool.common.enums.RideRequestStatus;
import com.cabpool.entity.RideRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, UUID> {

    List<RideRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<RideRequest> findByStatusAndVenueAndDirection(RideRequestStatus status, String venue, Direction direction);

    @Query("SELECT r FROM RideRequest r WHERE r.status = 'OPEN' AND r.venue = :venue " +
           "AND r.direction = :direction AND r.targetTime BETWEEN :minTime AND :maxTime ORDER BY r.targetTime ASC")
    List<RideRequest> findOpenRequestsInTimeWindow(
            @Param("venue") String venue,
            @Param("direction") Direction direction,
            @Param("minTime") OffsetDateTime minTime,
            @Param("maxTime") OffsetDateTime maxTime
    );

    Optional<RideRequest> findByEventIdentifierAndStatus(String eventIdentifier, RideRequestStatus status);

    long countByStatus(RideRequestStatus status);
}

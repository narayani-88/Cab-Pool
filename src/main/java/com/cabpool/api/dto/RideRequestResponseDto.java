package com.cabpool.api.dto;

import com.cabpool.common.enums.Direction;
import com.cabpool.common.enums.RideRequestPreference;
import com.cabpool.common.enums.RideRequestStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RideRequestResponseDto {
    private UUID id;
    private UUID userId;
    private String venue;
    private Direction direction;
    private String eventIdentifier;
    private OffsetDateTime targetTime;
    private Integer maxWaitMinutes;
    private String dropAddress;
    private Double dropLat;
    private Double dropLng;
    private String dropZone;
    private RideRequestPreference preference;
    private RideRequestStatus status;
    private Long version;
    private OffsetDateTime createdAt;
    private Long measuredIngestionTimeMs;
    private boolean alreadyActive;

    public RideRequestResponseDto() {}

    public RideRequestResponseDto(UUID id, UUID userId, String venue, Direction direction, String eventIdentifier,
                                 OffsetDateTime targetTime, Integer maxWaitMinutes, String dropAddress,
                                 Double dropLat, Double dropLng, String dropZone, RideRequestPreference preference,
                                 RideRequestStatus status, Long version, OffsetDateTime createdAt,
                                 Long measuredIngestionTimeMs, boolean alreadyActive) {
        this.id = id;
        this.userId = userId;
        this.venue = venue;
        this.direction = direction;
        this.eventIdentifier = eventIdentifier;
        this.targetTime = targetTime;
        this.maxWaitMinutes = maxWaitMinutes;
        this.dropAddress = dropAddress;
        this.dropLat = dropLat;
        this.dropLng = dropLng;
        this.dropZone = dropZone;
        this.preference = preference;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.measuredIngestionTimeMs = measuredIngestionTimeMs;
        this.alreadyActive = alreadyActive;
    }

    public static RideRequestResponseDtoBuilder builder() {
        return new RideRequestResponseDtoBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public String getEventIdentifier() { return eventIdentifier; }
    public void setEventIdentifier(String eventIdentifier) { this.eventIdentifier = eventIdentifier; }
    public OffsetDateTime getTargetTime() { return targetTime; }
    public void setTargetTime(OffsetDateTime targetTime) { this.targetTime = targetTime; }
    public Integer getMaxWaitMinutes() { return maxWaitMinutes; }
    public void setMaxWaitMinutes(Integer maxWaitMinutes) { this.maxWaitMinutes = maxWaitMinutes; }
    public String getDropAddress() { return dropAddress; }
    public void setDropAddress(String dropAddress) { this.dropAddress = dropAddress; }
    public Double getDropLat() { return dropLat; }
    public void setDropLat(Double dropLat) { this.dropLat = dropLat; }
    public Double getDropLng() { return dropLng; }
    public void setDropLng(Double dropLng) { this.dropLng = dropLng; }
    public String getDropZone() { return dropZone; }
    public void setDropZone(String dropZone) { this.dropZone = dropZone; }
    public RideRequestPreference getPreference() { return preference; }
    public void setPreference(RideRequestPreference preference) { this.preference = preference; }
    public RideRequestStatus getStatus() { return status; }
    public void setStatus(RideRequestStatus status) { this.status = status; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public Long getMeasuredIngestionTimeMs() { return measuredIngestionTimeMs; }
    public void setMeasuredIngestionTimeMs(Long measuredIngestionTimeMs) { this.measuredIngestionTimeMs = measuredIngestionTimeMs; }
    public boolean isAlreadyActive() { return alreadyActive; }
    public void setAlreadyActive(boolean alreadyActive) { this.alreadyActive = alreadyActive; }

    public static class RideRequestResponseDtoBuilder {
        private UUID id;
        private UUID userId;
        private String venue;
        private Direction direction;
        private String eventIdentifier;
        private OffsetDateTime targetTime;
        private Integer maxWaitMinutes;
        private String dropAddress;
        private Double dropLat;
        private Double dropLng;
        private String dropZone;
        private RideRequestPreference preference;
        private RideRequestStatus status;
        private Long version;
        private OffsetDateTime createdAt;
        private Long measuredIngestionTimeMs;
        private boolean alreadyActive;

        public RideRequestResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public RideRequestResponseDtoBuilder userId(UUID userId) { this.userId = userId; return this; }
        public RideRequestResponseDtoBuilder venue(String venue) { this.venue = venue; return this; }
        public RideRequestResponseDtoBuilder direction(Direction direction) { this.direction = direction; return this; }
        public RideRequestResponseDtoBuilder eventIdentifier(String eventIdentifier) { this.eventIdentifier = eventIdentifier; return this; }
        public RideRequestResponseDtoBuilder targetTime(OffsetDateTime targetTime) { this.targetTime = targetTime; return this; }
        public RideRequestResponseDtoBuilder maxWaitMinutes(Integer maxWaitMinutes) { this.maxWaitMinutes = maxWaitMinutes; return this; }
        public RideRequestResponseDtoBuilder dropAddress(String dropAddress) { this.dropAddress = dropAddress; return this; }
        public RideRequestResponseDtoBuilder dropLat(Double dropLat) { this.dropLat = dropLat; return this; }
        public RideRequestResponseDtoBuilder dropLng(Double dropLng) { this.dropLng = dropLng; return this; }
        public RideRequestResponseDtoBuilder dropZone(String dropZone) { this.dropZone = dropZone; return this; }
        public RideRequestResponseDtoBuilder preference(RideRequestPreference preference) { this.preference = preference; return this; }
        public RideRequestResponseDtoBuilder status(RideRequestStatus status) { this.status = status; return this; }
        public RideRequestResponseDtoBuilder version(Long version) { this.version = version; return this; }
        public RideRequestResponseDtoBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public RideRequestResponseDtoBuilder measuredIngestionTimeMs(Long measuredIngestionTimeMs) { this.measuredIngestionTimeMs = measuredIngestionTimeMs; return this; }
        public RideRequestResponseDtoBuilder alreadyActive(boolean alreadyActive) { this.alreadyActive = alreadyActive; return this; }

        public RideRequestResponseDto build() {
            return new RideRequestResponseDto(id, userId, venue, direction, eventIdentifier, targetTime, maxWaitMinutes, dropAddress, dropLat, dropLng, dropZone, preference, status, version, createdAt, measuredIngestionTimeMs, alreadyActive);
        }
    }
}

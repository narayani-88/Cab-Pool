package com.cabpool.api.dto;

import com.cabpool.common.enums.Direction;
import com.cabpool.common.enums.RideRequestPreference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CreateRideRequestDto {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotBlank(message = "venue is required (e.g. KIA_AIRPORT, CHINNASWAMY_STADIUM, MANYATA_TECH_PARK)")
    private String venue;

    private Direction direction = Direction.FROM_VENUE;

    private String eventIdentifier;

    @NotNull(message = "targetTime is required")
    private OffsetDateTime targetTime;

    private Integer maxWaitMinutes = 30;

    @NotBlank(message = "dropAddress is required")
    private String dropAddress;

    @NotNull(message = "dropLat is required")
    private Double dropLat;

    @NotNull(message = "dropLng is required")
    private Double dropLng;

    @NotBlank(message = "dropZone is required (e.g. HSR, WHITEFIELD, INDIRANAGAR)")
    private String dropZone;

    private RideRequestPreference preference = RideRequestPreference.MIXED;

    public CreateRideRequestDto() {}

    public CreateRideRequestDto(UUID userId, String venue, Direction direction, String eventIdentifier,
                                OffsetDateTime targetTime, Integer maxWaitMinutes, String dropAddress,
                                Double dropLat, Double dropLng, String dropZone, RideRequestPreference preference) {
        this.userId = userId;
        this.venue = venue;
        this.direction = direction != null ? direction : Direction.FROM_VENUE;
        this.eventIdentifier = eventIdentifier;
        this.targetTime = targetTime;
        this.maxWaitMinutes = maxWaitMinutes != null ? maxWaitMinutes : 30;
        this.dropAddress = dropAddress;
        this.dropLat = dropLat;
        this.dropLng = dropLng;
        this.dropZone = dropZone;
        this.preference = preference != null ? preference : RideRequestPreference.MIXED;
    }

    public static CreateRideRequestDtoBuilder builder() {
        return new CreateRideRequestDtoBuilder();
    }

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

    public static class CreateRideRequestDtoBuilder {
        private UUID userId;
        private String venue;
        private Direction direction = Direction.FROM_VENUE;
        private String eventIdentifier;
        private OffsetDateTime targetTime;
        private Integer maxWaitMinutes = 30;
        private String dropAddress;
        private Double dropLat;
        private Double dropLng;
        private String dropZone;
        private RideRequestPreference preference = RideRequestPreference.MIXED;

        public CreateRideRequestDtoBuilder userId(UUID userId) { this.userId = userId; return this; }
        public CreateRideRequestDtoBuilder venue(String venue) { this.venue = venue; return this; }
        public CreateRideRequestDtoBuilder direction(Direction direction) { this.direction = direction; return this; }
        public CreateRideRequestDtoBuilder eventIdentifier(String eventIdentifier) { this.eventIdentifier = eventIdentifier; return this; }
        public CreateRideRequestDtoBuilder targetTime(OffsetDateTime targetTime) { this.targetTime = targetTime; return this; }
        public CreateRideRequestDtoBuilder maxWaitMinutes(Integer maxWaitMinutes) { this.maxWaitMinutes = maxWaitMinutes; return this; }
        public CreateRideRequestDtoBuilder dropAddress(String dropAddress) { this.dropAddress = dropAddress; return this; }
        public CreateRideRequestDtoBuilder dropLat(Double dropLat) { this.dropLat = dropLat; return this; }
        public CreateRideRequestDtoBuilder dropLng(Double dropLng) { this.dropLng = dropLng; return this; }
        public CreateRideRequestDtoBuilder dropZone(String dropZone) { this.dropZone = dropZone; return this; }
        public CreateRideRequestDtoBuilder preference(RideRequestPreference preference) { this.preference = preference; return this; }

        public CreateRideRequestDto build() {
            return new CreateRideRequestDto(userId, venue, direction, eventIdentifier, targetTime, maxWaitMinutes, dropAddress, dropLat, dropLng, dropZone, preference);
        }
    }
}

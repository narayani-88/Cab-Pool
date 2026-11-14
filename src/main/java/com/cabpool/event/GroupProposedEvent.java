package com.cabpool.event;

import com.cabpool.common.enums.Direction;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GroupProposedEvent implements Serializable {

    private UUID groupId;
    private String venue;
    private Direction direction;
    private boolean womenOnly;
    private OffsetDateTime plannedPickupTime;
    private BigDecimal estTotalFare;
    private List<MemberSummary> members;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class MemberSummary implements Serializable {
        private UUID requestId;
        private UUID userId;
        private Integer dropOrder;
        private BigDecimal shareAmount;
        private String dropZone;
        private String dropAddress;
    }
}

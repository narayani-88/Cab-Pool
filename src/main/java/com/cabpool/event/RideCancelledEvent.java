package com.cabpool.event;

import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RideCancelledEvent implements Serializable {
    private UUID cancelledRequestId;
    private UUID cancelledUserId;
    private String reason;
    private UUID previousGroupId;
    private List<UUID> remainingRequestIdsToRematch;
}

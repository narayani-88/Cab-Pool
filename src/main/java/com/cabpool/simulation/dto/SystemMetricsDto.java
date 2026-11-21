package com.cabpool.simulation.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMetricsDto {
    private long totalUsers;
    private long totalRequests;
    private long openRequests;
    private long matchedRequests;
    private long confirmedRequests;
    private long cancelledRequests;
    private long totalGroups;
    private long confirmedGroups;
    private int activeRedisPoolCandidates;
    private Map<String, Integer> poolByZone;
    private double avgIngestionLatencyMs;
}

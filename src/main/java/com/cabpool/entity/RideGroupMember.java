package com.cabpool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ride_group_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_request_id", columnNames = "request_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideGroupMember {

    @EmbeddedId
    private RideGroupMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private RideGroup rideGroup;

    @Column(name = "request_id", insertable = false, updatable = false, nullable = false)
    private UUID requestId;

    @Column(name = "drop_order")
    private Integer dropOrder;

    @Column(name = "share_amount", precision = 10, scale = 2)
    private BigDecimal shareAmount;

    @Column(nullable = false)
    @Builder.Default
    private boolean accepted = false;
}

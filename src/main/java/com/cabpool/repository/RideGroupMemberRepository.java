package com.cabpool.repository;

import com.cabpool.entity.RideGroupMember;
import com.cabpool.entity.RideGroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideGroupMemberRepository extends JpaRepository<RideGroupMember, RideGroupMemberId> {

    Optional<RideGroupMember> findByRequestId(UUID requestId);

    List<RideGroupMember> findByRideGroupId(UUID groupId);
}

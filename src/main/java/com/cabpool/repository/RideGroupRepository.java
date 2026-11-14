package com.cabpool.repository;

import com.cabpool.common.enums.RideGroupStatus;
import com.cabpool.entity.RideGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RideGroupRepository extends JpaRepository<RideGroup, UUID> {
    List<RideGroup> findByStatus(RideGroupStatus status);
}

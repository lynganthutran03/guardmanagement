package com.lytran.guardmanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.entity.LeaveRequest;
import com.lytran.guardmanagement.model.LeaveStatus;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByGuardIdOrderByRequestedAtDesc(Long guardId);

    List<LeaveRequest> findByStatusOrderByRequestedAtAsc(LeaveStatus status);

    boolean existsByGuardIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long guardId, LeaveStatus status, LocalDate date, LocalDate dateAgain
    );
}

package com.lytran.guardmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.entity.ShiftHistory;

public interface ShiftHistoryRepository extends JpaRepository<ShiftHistory, Long>{
    boolean existsByShiftId(Long shiftId);
}

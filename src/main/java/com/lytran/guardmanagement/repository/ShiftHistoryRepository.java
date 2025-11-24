package com.lytran.guardmanagement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.entity.ShiftHistory;

public interface ShiftHistoryRepository extends JpaRepository<ShiftHistory, Long>{
    boolean existsByShiftId(Long shiftId);

    List<ShiftHistory> findAllByShift_ShiftDateBetween(LocalDate startDate, LocalDate endDate);

    List<ShiftHistory> findAllByShift_Guard_IdAndShift_ShiftDateBetween(Long guardId, LocalDate startDate, LocalDate endDate);

    Optional<ShiftHistory> findByShiftId(Long shiftId);
}

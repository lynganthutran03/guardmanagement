package com.lytran.guardmanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.model.Block;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.model.TimeSlot;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    boolean existsByShiftDateAndTimeSlotAndBlock(LocalDate date, TimeSlot timeSlot, Block block);

    boolean existsByUserIdAndShiftDateAndAcceptedTrue(Long userId, LocalDate date);

    List<Shift> findByUserIdAndShiftDate(Long userId, LocalDate date);

    List<Shift> findByUserIdAndShiftDateAndAcceptedTrue(Long userId, LocalDate date);

    List<Shift> findByUserIdAndShiftDateBeforeAndAcceptedTrue(Long userId, LocalDate date);

    List<Shift> findByUserIdAndAcceptedTrue(Long userId);
}

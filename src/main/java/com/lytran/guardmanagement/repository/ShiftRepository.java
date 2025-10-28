package com.lytran.guardmanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.model.Location;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.model.TimeSlot;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    boolean existsByGuardIdAndShiftDate(Long guardId, LocalDate date);

    boolean existsByShiftDateAndTimeSlotAndLocation(LocalDate date, TimeSlot timeSlot, Location location);

    List<Shift> findByGuardIdAndShiftDate(Long guardId, LocalDate date);

    List<Shift> findByGuardIdAndShiftDateBefore(Long guardId, LocalDate date);

    List<Shift> findByGuardId(Long guardId);

    boolean existsByGuardIdAndShiftDateBetween(Long guardId, LocalDate startDate, LocalDate endDate);
}
package com.lytran.guardmanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lytran.guardmanagement.entity.Location;
import com.lytran.guardmanagement.entity.TimeSlot;
import com.lytran.guardmanagement.model.Shift;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    boolean existsByGuardIdAndShiftDate(Long guardId, LocalDate date);

    boolean existsByShiftDateAndTimeSlotAndLocation(LocalDate date, TimeSlot timeSlot, Location location);

    boolean existsByGuardIdAndShiftDateBetween(Long guardId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(s) > 0 FROM Shift s WHERE s.guard.team = :team AND s.shiftDate BETWEEN :startDate AND :endDate")
    boolean existsByTeamAndDateRange(@Param("team") String team, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Shift> findByGuardIdAndShiftDate(Long guardId, LocalDate date);

    List<Shift> findByGuardIdAndShiftDateBefore(Long guardId, LocalDate date);

    List<Shift> findByGuardId(Long guardId);

    List<Shift> findByGuardIsNotNullAndShiftDateBeforeOrderByShiftDateDesc(LocalDate date);

    List<Shift> findAllByGuardIdAndShiftDateBetween(Long guardId, LocalDate startDate, LocalDate endDate);

    List<Shift> findByGuardIsNullAndShiftDateGreaterThanEqualOrderByShiftDateAsc(LocalDate date);
}

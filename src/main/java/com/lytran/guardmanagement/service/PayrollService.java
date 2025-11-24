package com.lytran.guardmanagement.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.dto.PayrollDTO;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.ShiftHistory;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.ShiftHistoryRepository;

@Service
public class PayrollService {

    @Autowired
    private ShiftHistoryRepository shiftHistoryRepository;

    @Autowired
    private GuardRepository guardRepository;

    public List<PayrollDTO> calculatePayroll(int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<ShiftHistory> histories = shiftHistoryRepository.findAllByShift_ShiftDateBetween(startDate, endDate);
        Map<Guard, List<ShiftHistory>> historyByGuard = histories.stream().collect(Collectors.groupingBy(h -> h.getShift().getGuard()));

        List<PayrollDTO> payrolls = new ArrayList<>();
        List<Guard> allGuards = guardRepository.findAll();

        for (Guard guard : allGuards) {
            List<ShiftHistory> guardWork = historyByGuard.getOrDefault(guard, new ArrayList<>());
            int totalShifts = guardWork.size();
            int lateCount = 0;
            double totalHours = 0;

            for (ShiftHistory h : guardWork) {
                long seconds = Duration.between(
                        h.getShift().getTimeSlot().getStartTime(),
                        h.getShift().getTimeSlot().getEndTime()
                ).getSeconds();

                double hours = seconds / 3600.0;
                totalHours += hours;

                if ("LATE".equals(h.getAttendanceStatus())) {
                    lateCount++;
                }
            }

            double rate = guard.getSalaryPerHour() != null ? guard.getSalaryPerHour() : 25000.0;
            double totalSalary = totalHours * rate;

            payrolls.add(new PayrollDTO(
                    guard.getId(),
                    guard.getFullName(),
                    guard.getIdentityNumber(),
                    totalShifts,
                    Math.round(totalHours * 100.0) / 100.0,
                    totalSalary, lateCount
            ));
        }

        return payrolls;
    }

    public PayrollDTO calculatePayrollForGuard(String username, int month, int year) {
        Guard guard = guardRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảo vệ."));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<ShiftHistory> histories = shiftHistoryRepository.findAllByShift_Guard_IdAndShift_ShiftDateBetween(guard.getId(), startDate, endDate);

        int totalShifts = histories.size();
        int lateCount = 0;
        double totalHours = 0;

        for (ShiftHistory h : histories) {
            long seconds = Duration.between(
                    h.getShift().getTimeSlot().getStartTime(),
                    h.getShift().getTimeSlot().getEndTime()).getSeconds();

            double hours = seconds / 3600.0;
            totalHours += hours;

            if ("LATE".equals(h.getAttendanceStatus())) {
                lateCount++;
            }
        }

        double rate = guard.getSalaryPerHour() != null ? guard.getSalaryPerHour() : 25000.0;
        double totalSalary = totalHours * rate;

        return new PayrollDTO(
                guard.getId(),
                guard.getFullName(),
                guard.getIdentityNumber(),
                totalShifts,
                Math.round(totalHours * 100.0) / 100.0,
                totalSalary,
                lateCount
        );
    }
}

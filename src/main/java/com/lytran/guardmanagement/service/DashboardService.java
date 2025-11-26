package com.lytran.guardmanagement.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.dto.GuardDashboardDTO;
import com.lytran.guardmanagement.dto.LeaveRequestResponseDTO;
import com.lytran.guardmanagement.dto.ManagerDashboardDTO;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.LeaveRequest;
import com.lytran.guardmanagement.model.LeaveStatus;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.LeaveRequestRepository;
import com.lytran.guardmanagement.repository.ShiftHistoryRepository;
import com.lytran.guardmanagement.repository.ShiftRepository;

@Service
public class DashboardService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private ShiftHistoryRepository shiftHistoryRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    public GuardDashboardDTO getGuardStats(String username) {
        Guard guard = guardRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảo vệ"));

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        List<Shift> monthlyShifts = shiftRepository.findByGuardIdAndShiftDateBetween(guard.getId(), firstDayOfMonth, lastDayOfMonth);
        long dayShifts = 0;
        long nightShifts = 0;

        Map<String, Long> weeklyCounts = new LinkedHashMap<>();
        weeklyCounts.put("Tuần 1", 0L);
        weeklyCounts.put("Tuần 2", 0L);
        weeklyCounts.put("Tuần 3", 0L);
        weeklyCounts.put("Tuần 4", 0L);

        for (Shift s : monthlyShifts) {
            if (s.getTimeSlot().getName().contains("DAY")) {
                dayShifts++;
            } else {
                nightShifts++;
            }

            int dayOfMonth = s.getShiftDate().getDayOfMonth();
            if (dayOfMonth <= 7) {
                weeklyCounts.put("Tuần 1", weeklyCounts.get("Tuần 1") + 1);
            } else if (dayOfMonth <= 14) {
                weeklyCounts.put("Tuần 2", weeklyCounts.get("Tuần 2") + 1);
            } else if (dayOfMonth <= 21) {
                weeklyCounts.put("Tuần 3", weeklyCounts.get("Tuần 3") + 1);
            } else {
                weeklyCounts.put("Tuần 4", weeklyCounts.get("Tuần 4") + 1);
            }
        }

        Map<String, Long> absenceCounts = new LinkedHashMap<>();
        absenceCounts.put("Tháng " + today.minusMonths(2).getMonthValue(), 0L);
        absenceCounts.put("Tháng " + today.minusMonths(1).getMonthValue(), 0L);
        absenceCounts.put("Tháng " + today.getMonthValue(), 0L);

        return new GuardDashboardDTO(dayShifts, nightShifts, weeklyCounts, absenceCounts);
    }

    public ManagerDashboardDTO getManagerStats() {
        ManagerDashboardDTO stats = new ManagerDashboardDTO();
        LocalDate today = LocalDate.now();

        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endDate = today;

        List<Shift> shiftsInRange = shiftRepository.findAllByShiftDateBetween(firstDayOfMonth, endDate);
        long totalShifts = shiftsInRange.size();
        long present = 0;
        long late = 0;

        for (Shift s : shiftsInRange) {
            var history = shiftHistoryRepository.findByShiftId(s.getId());

            if (history.isPresent()) {
                if ("LATE".equals(history.get().getAttendanceStatus())) {
                    late++;
                } else {
                    present++;
                }
            }
        }

        stats.setPresentCount(present);
        stats.setLateCount(late);
        stats.setAbsenceCount(totalShifts - present - late);

        LocalDate startWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endWeek = startWeek.plusDays(6);

        List<Shift> weekShifts = shiftRepository.findAllByShiftDateBetween(startWeek, endWeek);
        long assignedCount = weekShifts.stream().filter(s -> s.getGuard() != null).count();

        stats.setAssignedShifts(assignedCount);
        stats.setOpenShifts(0);

        List<LeaveRequest> leaves = leaveRequestRepository.findByStatusOrderByRequestedAtAsc(LeaveStatus.PENDING);
        List<LeaveRequestResponseDTO> upcoming = leaves.stream()
                .limit(5)
                .map(LeaveRequestResponseDTO::new)
                .collect(Collectors.toList());
        stats.setUpcomingLeaves(upcoming);
        return stats;
    }
}

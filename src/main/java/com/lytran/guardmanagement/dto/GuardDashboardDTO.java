package com.lytran.guardmanagement.dto;

import java.util.Map;

public class GuardDashboardDTO {
    private long totalDayShifts;
    private long totalNightShifts;
    private Map<String, Long> weeklyShiftCounts;
    private Map<String, Long> monthlyAbsenceCounts;

    public GuardDashboardDTO(long totalDayShifts, long totalNightShifts, Map<String, Long> weeklyShiftCounts,
            Map<String, Long> monthlyAbsenceCounts) {
        this.totalDayShifts = totalDayShifts;
        this.totalNightShifts = totalNightShifts;
        this.weeklyShiftCounts = weeklyShiftCounts;
        this.monthlyAbsenceCounts = monthlyAbsenceCounts;
    }

    public long getTotalDayShifts() {
        return totalDayShifts;
    }

    public void setTotalDayShifts(long totalDayShifts) {
        this.totalDayShifts = totalDayShifts;
    }

    public long getTotalNightShifts() {
        return totalNightShifts;
    }

    public void setTotalNightShifts(long totalNightShifts) {
        this.totalNightShifts = totalNightShifts;
    }

    public Map<String, Long> getWeeklyShiftCounts() {
        return weeklyShiftCounts;
    }

    public void setWeeklyShiftCounts(Map<String, Long> weeklyShiftCounts) {
        this.weeklyShiftCounts = weeklyShiftCounts;
    }

    public Map<String, Long> getMonthlyAbsenceCounts() {
        return monthlyAbsenceCounts;
    }

    public void setMonthlyAbsenceCounts(Map<String, Long> monthlyAbsenceCounts) {
        this.monthlyAbsenceCounts = monthlyAbsenceCounts;
    }
}

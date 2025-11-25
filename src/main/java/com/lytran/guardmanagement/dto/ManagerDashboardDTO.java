package com.lytran.guardmanagement.dto;

import java.util.List;

public class ManagerDashboardDTO {
    private long presentCount;
    private long lateCount;
    private long absenceCount;
    private long assignedShifts;
    private long openShifts;
    private List<LeaveRequestResponseDTO> upcomingLeaves;

    public ManagerDashboardDTO() {}

    public long getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }

    public long getLateCount() {
        return lateCount;
    }

    public void setLateCount(long lateCount) {
        this.lateCount = lateCount;
    }

    public long getAbsenceCount() {
        return absenceCount;
    }

    public void setAbsenceCount(long absenceCount) {
        this.absenceCount = absenceCount;
    }

    public long getAssignedShifts() {
        return assignedShifts;
    }

    public void setAssignedShifts(long assignedShifts) {
        this.assignedShifts = assignedShifts;
    }

    public long getOpenShifts() {
        return openShifts;
    }

    public void setOpenShifts(long openShifts) {
        this.openShifts = openShifts;
    }

    public List<LeaveRequestResponseDTO> getUpcomingLeaves() {
        return upcomingLeaves;
    }

    public void setUpcomingLeaves(List<LeaveRequestResponseDTO> upcomingLeaves) {
        this.upcomingLeaves = upcomingLeaves;
    }
}

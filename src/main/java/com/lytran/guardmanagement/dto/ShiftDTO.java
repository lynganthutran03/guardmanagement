package com.lytran.guardmanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ShiftDTO {
    private Long id;
    private LocalDate shiftDate;
    private String timeSlot;
    private String location;
    private Long guardId;
    private String guardName;
    private String guardIdentityNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private String attendanceStatus;
    private LocalDateTime checkInTime;

    public ShiftDTO(Long id, LocalDate shiftDate, String timeSlot, String location, Long guardId) {
        this.id = id;
        this.shiftDate = shiftDate;
        this.timeSlot = timeSlot;
        this.location = location;
        this.guardId = guardId;
    }

    public ShiftDTO() {
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getGuardId() {
        return guardId;
    }

    public void setGuardId(Long guardId) {
        this.guardId = guardId;
    }

    public String getGuardName() {
        return guardName;
    }

    public void setGuardName(String guardName) {
        this.guardName = guardName;
    }

    public String getGuardIdentityNumber() {
        return guardIdentityNumber;
    }

    public void setGuardIdentityNumber(String guardIdentityNumber) {
        this.guardIdentityNumber = guardIdentityNumber;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
}

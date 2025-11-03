package com.lytran.guardmanagement.dto;

import java.time.LocalDate;

public class ShiftDTO {
    private Long id;
    private LocalDate shiftDate;
    private String timeSlot;
    private String location;
    private Long guardId;
    private String guardName;
    private String guardIdentityNumber;

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
}

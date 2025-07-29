package com.lytran.guardmanagement.dto;

import java.time.LocalDate;

public class ShiftDTO {
    private Long id;
    private LocalDate shiftDate;
    private String timeSlot;
    private String block;
    private Long employeeId;

    public ShiftDTO(Long id, LocalDate shiftDate, String timeSlot, String block, Long employeeId) {
        this.id = id;
        this.shiftDate = shiftDate;
        this.timeSlot = timeSlot;
        this.block = block;
        this.employeeId = employeeId;
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

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
}

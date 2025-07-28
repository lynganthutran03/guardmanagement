package com.lytran.guardmanagement.dto;

import java.time.LocalDate;

import com.lytran.guardmanagement.model.Block;
import com.lytran.guardmanagement.model.TimeSlot;

public class ShiftRequest {
    private Long userId;     
    private TimeSlot timeSlot;
    private Block block;
    private LocalDate shiftDate;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public TimeSlot getTimeSlot() {
        return timeSlot;
    }
    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }
    public Block getBlock() {
        return block;
    }
    public void setBlock(Block block) {
        this.block = block;
    }
    public LocalDate getShiftDate() {
        return shiftDate;
    }
    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }
}
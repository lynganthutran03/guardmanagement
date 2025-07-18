package com.lytran.guardmanagement.dto;

import java.time.LocalDate;

import com.lytran.guardmanagement.model.Block;
import com.lytran.guardmanagement.model.TimeSlot;

public class ShiftRequest {
    private TimeSlot timeSlot;
    private Block block;
    private LocalDate shiftDate;

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
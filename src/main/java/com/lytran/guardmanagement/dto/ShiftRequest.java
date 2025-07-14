package com.lytran.guardmanagement.dto;

import com.lytran.guardmanagement.model.Block;
import com.lytran.guardmanagement.model.TimeSlot;

public class ShiftRequest {
    private TimeSlot timeSlot;
    private Block block;

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
}
package com.lytran.guardmanagement.dto;

import java.time.LocalDate;

import com.lytran.guardmanagement.model.Location;
import com.lytran.guardmanagement.model.TimeSlot;

public record AcceptedShiftDTO (
    Long id,
    LocalDate shiftDate,
    TimeSlot timeSlot,
    Location location
) {}
    

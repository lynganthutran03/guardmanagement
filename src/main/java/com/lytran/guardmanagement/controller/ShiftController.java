package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lytran.guardmanagement.dto.ShiftRequest;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.service.ShiftService;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    // 👩‍💼 Manager creates shift for a guard
    @PostMapping("/create")
    public ResponseEntity<?> createShift(@RequestBody ShiftRequest req) {
        try {
            Shift shift = shiftService.createShift(
                    req.getUserId(), // manager must send target guard ID
                    req.getTimeSlot(),
                    req.getBlock(),
                    req.getShiftDate()
            );
            return ResponseEntity.ok(shift);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // 👮 Guard views shifts for a specific date
    @GetMapping
    public List<Shift> getShiftsByDate(@RequestParam("date") LocalDate date, Principal principal) {
        Long guardId = shiftService.getGuardIdByUsername(principal.getName());
        return shiftService.getShiftsForGuardByDate(guardId, date);
    }

    // 📆 Guard gets shift history before today
    @GetMapping("/history")
    public List<Shift> getShiftHistory(Principal principal) {
        Long guardId = shiftService.getGuardIdByUsername(principal.getName());
        return shiftService.getShiftsBeforeToday(guardId);
    }

    // 📅 Guard sees all their shifts
    @GetMapping("/calendar")
    public List<Shift> getAllShifts(Principal principal) {
        Long guardId = shiftService.getGuardIdByUsername(principal.getName());
        return shiftService.getAllShiftsForGuard(guardId);
    }
}

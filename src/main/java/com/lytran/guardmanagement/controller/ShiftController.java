package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.ShiftDTO;
import com.lytran.guardmanagement.dto.ShiftRequest;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.service.ShiftService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping("/manager/shifts")
    public ResponseEntity<?> createShiftByManager(@RequestBody ShiftRequest request, Principal principal) {
        try {
            Shift shift = shiftService.createShiftByManager(request, principal.getName());
            return ResponseEntity.ok(shift);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/manager/shifts")
    public List<ShiftDTO> getShiftsForGuardOnDate(@RequestParam("guardId") Long guardId,
            @RequestParam("date") LocalDate date) {
        return shiftService.getShiftsForGuardByDate(guardId, date);
    }

    @PostMapping("/manager/shifts/assign")
    public ResponseEntity<?> assignShift(@RequestBody Map<String, Long> payload) {
        Long shiftId = payload.get("shiftId");
        Long guardId = payload.get("guardId");

        try {
            shiftService.assignShiftToGuard(shiftId, guardId);
            return ResponseEntity.ok(Map.of("message", "Shift assigned"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/manager/schedule/generate-week")
    public ResponseEntity<?> generateWeekSchedule(@RequestBody Map<String, String> payload, Principal principal) {
        try {
            Long guardId = Long.parseLong(payload.get("guardId"));
            LocalDate weekStartDate = LocalDate.parse(payload.get("weekStartDate"));
            String managerUsername = principal.getName();

            shiftService.generateWeekForGuard(guardId, weekStartDate, managerUsername);
            return ResponseEntity.ok(Map.of("message", "Schedule generated for the week " + guardId));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid guardId format")); 
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid weekStartDate format")); 
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }

    }

    @GetMapping("/shifts")
    public List<ShiftDTO> getShiftsByDate(@RequestParam("date") LocalDate date, Principal principal) {
        Long guardId = shiftService.getGuardIdByUsername(principal.getName());
        return shiftService.getShiftsForGuardByDate(guardId, date);
    }

    @GetMapping("/shifts/history")
    public List<ShiftDTO> getShiftHistory(Principal principal) {
        Long guardId = shiftService.getGuardIdByUsername(principal.getName());
        return shiftService.getShiftHistory(guardId);
    }

    @GetMapping("/shifts/calendar")
    public List<ShiftDTO> getAllShifts(Principal principal) {
        Long guardId = shiftService.getGuardIdByUsername(principal.getName());
        return shiftService.getAllShifts(guardId);
    }

    @GetMapping("/shifts/accepted-today")
    public ResponseEntity<ShiftDTO> getTodayAcceptedShift(Principal principal) {
        Long guardId = shiftService.getGuardIdByUsername(principal.getName());
        return shiftService.getTodayAcceptedShift(guardId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

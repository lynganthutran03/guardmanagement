package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.time.DayOfWeek;
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

import com.lytran.guardmanagement.dto.GuardDTO;
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

    @PostMapping("/manager/schedule/generate-week-for-team")
    public ResponseEntity<?> generateWeekScheduleForTeam(
            @RequestBody Map<String, String> payload,
            Principal principal) {
        try {
            String team = payload.get("team");
            LocalDate weekStartDate = LocalDate.parse(payload.get("weekStartDate"));
            String managerUsername = principal.getName();

            if (team == null || (!team.equals("A") && !team.equals("B"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid or missing team. Must be 'A' or 'B'."));
            }

            if (weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Week start date must be a Monday."));
            }

            shiftService.generateWeekForTeam(team, weekStartDate, managerUsername);

            return ResponseEntity.ok(Map.of("message", "Schedule generated successfully for Team " + team + " for week starting " + weekStartDate));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid weekStartDate format. Use YYYY-MM-DD."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("Error during team schedule generation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred during schedule generation. Check server logs."));
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid weekStartDate format. Use YYYY-MM-DD."));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("lịch làm việc trong tuần này")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
            }
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

    @GetMapping("/manager/all-shifts")
    public ResponseEntity<List<ShiftDTO>> getAllShiftsForManager() {
        List<ShiftDTO> shifts = shiftService.getAllShiftsForManager();
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/manager/guards/available")
    public ResponseEntity<List<GuardDTO>> getAvailableGuards(@RequestParam("date") LocalDate date) {
        List<GuardDTO> availableGuards = shiftService.getAvailableGuardsForShift(date);
        return ResponseEntity.ok(availableGuards);
    }

    @GetMapping("/manager/shifts/by-guard")
    public ResponseEntity<List<ShiftDTO>> getShiftsForGuardInRange(
            @RequestParam("guardId") Long guardId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        
        List<ShiftDTO> shifts = shiftService.getShiftsForGuardInRange(guardId, startDate, endDate);
        return ResponseEntity.ok(shifts);
    }
}

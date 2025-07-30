package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.time.LocalDate;
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
    public List<ShiftDTO> getShiftsForGuardOnDate(@RequestParam("employeeId") Long employeeId,
            @RequestParam("date") LocalDate date) {
        return shiftService.getShiftsForEmployeeByDate(employeeId, date);
    }

    @PostMapping("/manager/shifts/assign")
    public ResponseEntity<?> assignShift(@RequestBody Map<String, Long> payload) {
        Long shiftId = payload.get("shiftId");
        Long employeeId = payload.get("employeeId");

        try {
            shiftService.assignShiftToEmployee(shiftId, employeeId);
            return ResponseEntity.ok(Map.of("message", "Shift assigned"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/shifts")
    public List<ShiftDTO> getShiftsByDate(@RequestParam("date") LocalDate date, Principal principal) {
        Long employeeId = shiftService.getEmployeeIdByUsername(principal.getName());
        return shiftService.getShiftsForEmployeeByDate(employeeId, date);
    }

    @GetMapping("/shifts/history")
    public List<ShiftDTO> getShiftHistory(Principal principal) {
        Long employeeId = shiftService.getEmployeeIdByUsername(principal.getName());
        return shiftService.getShiftHistory(employeeId);
    }

    @GetMapping("/shifts/calendar")
    public List<ShiftDTO> getAllShifts(Principal principal) {
        Long employeeId = shiftService.getEmployeeIdByUsername(principal.getName());
        return shiftService.getAllShifts(employeeId);
    }

    @GetMapping("/shifts/accepted-today")
    public ResponseEntity<ShiftDTO> getTodayAcceptedShift(Principal principal) {
        Long employeeId = shiftService.getEmployeeIdByUsername(principal.getName());
        return shiftService.getTodayAcceptedShift(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

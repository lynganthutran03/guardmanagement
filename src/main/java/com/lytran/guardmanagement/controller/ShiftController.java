package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.AcceptedShiftDTO;
import com.lytran.guardmanagement.dto.ShiftRequest;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.security.CustomUserDetails;
import com.lytran.guardmanagement.service.ShiftService;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateShift(@RequestBody ShiftRequest req, Authentication auth) {
        try {
            CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
            Shift shift = shiftService.generateShiftForUser(
                    cud.getUser().getId(),
                    req.getTimeSlot(),
                    req.getBlock()
            );
            return ResponseEntity.ok(shift);
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/today")
    public List<Shift> getTodayShifts(Principal principal) {
        return shiftService.getGeneratedShiftsForUser(getUserIdFromPrincipal(principal), LocalDate.now());
    }

    @PostMapping("/{id}/accept")
    public void acceptShift(@PathVariable Long id) {
        shiftService.acceptShift(id);
    }

    @GetMapping("/accepted-today")
    public List<AcceptedShiftDTO> getAcceptedShiftsToday(Authentication auth) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        return shiftService.getGeneratedShiftsForUser(cud.getUser().getId(), LocalDate.now())
                .stream()
                .filter(Shift::isAccepted)
                .map(shift -> new AcceptedShiftDTO(
                shift.getId(),
                shift.getShiftDate(),
                shift.getTimeSlot(),
                shift.getBlock()
        ))
                .toList();
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        return shiftService.getUserIdByUsername(principal.getName());
    }
}

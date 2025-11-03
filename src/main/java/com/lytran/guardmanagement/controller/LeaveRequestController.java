package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.LeaveRequestDTO;
import com.lytran.guardmanagement.dto.LeaveRequestResponseDTO;
import com.lytran.guardmanagement.service.LeaveRequestService;

@RestController
@RequestMapping("/api/leave-requests")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class LeaveRequestController {
    @Autowired
    private LeaveRequestService leaveRequestService;

    @PostMapping("/request")
    public ResponseEntity<?> createLeaveRequest(@RequestBody LeaveRequestDTO requestDTO, Principal principal) {
        try {
            String guardUsername = principal.getName();
            LeaveRequestResponseDTO newRequest = leaveRequestService.createLeaveRequest(requestDTO, guardUsername);
            return new ResponseEntity<>(newRequest, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getGuardRequestHistory(Principal principal) {
        try {
            String guardUsername = principal.getName();
            List<LeaveRequestResponseDTO> history = leaveRequestService.getRequestHistoryForGuard(guardUsername);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestResponseDTO>> getPendingRequests() {
        List<LeaveRequestResponseDTO> pendingRequests = leaveRequestService.getPendingRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveRequest(@PathVariable Long id, Principal principal) {
        try {
            String managerUsername = principal.getName();
            LeaveRequestResponseDTO updatedRequest = leaveRequestService.approveRequest(id, managerUsername);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/deny/{id}")
    public ResponseEntity<?> denyRequest(@PathVariable Long id, Principal principal) {
        try {
            String managerUsername = principal.getName();
            LeaveRequestResponseDTO updatedRequest = leaveRequestService.denyRequest(id, managerUsername);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/approved")
    public ResponseEntity<List<LeaveRequestResponseDTO>> getApprovedRequests() {
        List<LeaveRequestResponseDTO> approvedRequests = leaveRequestService.getApprovedRequests();
        return ResponseEntity.ok(approvedRequests);
    }
}

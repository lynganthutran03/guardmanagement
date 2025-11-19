package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.service.AttendanceService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(Principal principal, HttpServletRequest request) {
        try {
            String clientIp = getClientIp(request);

            attendanceService.checkIn(principal.getName(), clientIp);

            return ResponseEntity.ok(Map.of("message", "Điểm danh thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}

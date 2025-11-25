package com.lytran.guardmanagement.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.GuardDashboardDTO;
import com.lytran.guardmanagement.dto.ManagerDashboardDTO;
import com.lytran.guardmanagement.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/guard")
    public ResponseEntity<GuardDashboardDTO> getGuardStats(Principal principal) {
        return ResponseEntity.ok(dashboardService.getGuardStats(principal.getName()));
    }

    @GetMapping("/manager")
    public ResponseEntity<ManagerDashboardDTO> getManagerStats() {
        return ResponseEntity.ok(dashboardService.getManagerStats());
    }
}

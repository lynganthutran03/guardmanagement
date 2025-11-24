package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.PayrollDTO;
import com.lytran.guardmanagement.service.PayrollService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PayrollController {
    
    @Autowired
    private PayrollService payrollService;

    @GetMapping("/manager/payroll")
    public ResponseEntity<List<PayrollDTO>> getPayroll (
                @RequestParam(defaultValue= "-1" ) int month,
                @RequestParam(defaultValue= "-1") int year) {
        if(month == -1 || year == -1) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        return ResponseEntity.ok(payrollService.calculatePayroll(month, year));
    }

    @GetMapping("/my-payroll")
    public ResponseEntity<PayrollDTO> getMyPayroll(
                @RequestParam(defaultValue= "-1") int month,
                @RequestParam(defaultValue= "-1") int year,
                Principal principal) {
        if(month == -1 || year == -1) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        PayrollDTO payroll = payrollService.calculatePayrollForGuard(principal.getName(), month, year);
        return ResponseEntity.ok(payroll);
    }
}

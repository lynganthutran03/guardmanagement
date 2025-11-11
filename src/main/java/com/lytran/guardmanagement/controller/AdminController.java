package com.lytran.guardmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.GuardDTO;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;

@RestController
@RequestMapping("/api/admin/guards")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public List<GuardDTO> getAllGuards() {
        return guardRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private GuardDTO convertToDTO(Guard guard) {
        GuardDTO dto = new GuardDTO();
        dto.setId(guard.getId());
        dto.setUsername(guard.getUsername());
        dto.setFullName(guard.getFullName());
        dto.setIdentityNumber(guard.getIdentityNumber());
        dto.setTeam(guard.getTeam());
        dto.setRotaGroup(guard.getRotaGroup());
        return dto;
    }

    @PostMapping
    public ResponseEntity<GuardDTO> createGuard(@RequestBody Guard guard) {
        Manager defaultManager = managerRepository.findByUsername("manager1")
                .orElseThrow(() -> new RuntimeException("Manager 'manager1' not found"));
        guard.setManager(defaultManager);

        guard.setPassword(passwordEncoder.encode(guard.getPassword()));
        guard.setRole(com.lytran.guardmanagement.entity.Role.GUARD);

        Guard savedGuard = guardRepository.save(guard);
        return ResponseEntity.ok(convertToDTO(savedGuard));
    }
}

package com.lytran.guardmanagement.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.ManagerDTO;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.repository.ManagerRepository;

@RestController
@RequestMapping("/api/admin/managers")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ManagerController {

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public List<ManagerDTO> getAllManagers() {
        return managerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ManagerDTO> createManager(@RequestBody Manager manager) {
        try {
            manager.setPassword(passwordEncoder.encode(manager.getPassword()));
            manager.setRole(com.lytran.guardmanagement.entity.Role.MANAGER);

            Manager savedManager = managerRepository.save(manager);
            return ResponseEntity.ok(convertToDTO(savedManager));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteManager(@PathVariable Long id) {
        try {
            managerRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Không thể xóa. Quản lý này có thể đang phụ trách một số bảo vệ."));
        }
    }

    private ManagerDTO convertToDTO(Manager manager) {
        ManagerDTO dto = new ManagerDTO();
        dto.setId(manager.getId());
        dto.setUsername(manager.getUsername());
        dto.setFullName(manager.getFullName());
        dto.setIdentityNumber(manager.getIdentityNumber());
        dto.setRole(manager.getRole().name());
        return dto;
    }
}

package com.lytran.guardmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.GuardDTO;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.repository.GuardRepository;

@RestController
@RequestMapping("/api/guards")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class GuardController {

    private final GuardRepository guardRepository;

    public GuardController(GuardRepository guardRepository) {
        this.guardRepository = guardRepository;
    }

    @GetMapping
    public List<GuardDTO> getAllGuards() {
        List<Guard> guards = guardRepository.findAll();

        return guards.stream()
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
}

package com.lytran.guardmanagement.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.NotificationDTO;
import com.lytran.guardmanagement.entity.Notification;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;
import com.lytran.guardmanagement.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @GetMapping("/mine")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(Principal principal) {
        String username = principal.getName();
        List<Notification> notifications = List.of();

        var guardOpt = guardRepository.findByUsername(username);
        if (guardOpt.isPresent()) {
            notifications = notificationService.getMyNotifications(guardOpt.get().getId(), null);
        } else {
            var managerOpt = managerRepository.findByUsername(username);
            if (managerOpt.isPresent()) {
                notifications = notificationService.getMyNotifications(null, managerOpt.get().getId());
            }
        }

        List<NotificationDTO> dtos = notifications.stream()
                .map(n -> new NotificationDTO(
                    n.getId(), 
                    n.getMessage(),
                    n.getCreatedAt().toString(), 
                    n.isRead()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/mark-read/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}

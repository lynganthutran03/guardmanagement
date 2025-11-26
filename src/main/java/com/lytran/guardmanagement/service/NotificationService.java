package com.lytran.guardmanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.entity.Notification;
import com.lytran.guardmanagement.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotificationForGuard(Guard guard, String message, Long leaveRequestId, Long shiftId) {
        Notification notif = new Notification(guard, message);
        notif.setLeaveRequestId(leaveRequestId);
        notif.setShiftId(shiftId);
        notificationRepository.save(notif);
    }

    public void createNotificationForManager(Manager manager, String message, Long leaveRequestId) {
        Notification notif = new Notification(manager, message);
        notif.setLeaveRequestId(leaveRequestId);
        notificationRepository.save(notif);
    }

    public List<Notification> getMyNotifications(Long guardId, Long managerId) {
        if (guardId != null) {
            return notificationRepository.findByGuardIdOrderByCreatedAtDesc(guardId);
        } else if (managerId != null) {
            return notificationRepository.findByManagerIdOrderByCreatedAtDesc(managerId);
        }
        return List.of();
    }

    public void markAsRead(Long notifId) {
        notificationRepository.findById(notifId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}

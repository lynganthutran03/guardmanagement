package com.lytran.guardmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByGuardIdAndIsReadFalse(Long guardId);

    long countByManagerIdAndIsReadFalse(long managerId);

    List<Notification> findByGuardIdOrderByCreatedAtDesc(Long guardId);

    List<Notification> findByManagerIdOrderByCreatedAtDesc(long managerId);
}

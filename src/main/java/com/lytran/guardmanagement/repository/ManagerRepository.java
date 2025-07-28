package com.lytran.guardmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.entity.Manager;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
    Optional<Manager> findByUsername(String username);
}

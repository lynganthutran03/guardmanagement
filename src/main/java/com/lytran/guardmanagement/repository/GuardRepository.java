package com.lytran.guardmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.entity.Guard;

public interface GuardRepository extends JpaRepository<Guard, Long> {
    Optional<Guard> findByUsername(String username);

    List<Guard> findByTeam(String team);
}

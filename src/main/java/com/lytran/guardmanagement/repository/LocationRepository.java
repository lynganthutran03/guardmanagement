package com.lytran.guardmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lytran.guardmanagement.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    
}

package com.lytran.guardmanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {
    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if(managerRepository.count() == 0) {
            Manager manager = new Manager(
                "manager1",
                passwordEncoder.encode("Manager123"),
                "Lý Trần Thu Ngân",
                "M001"
            );
            managerRepository.save(manager);
        }

        if(guardRepository.count() == 0) {
            for(int i = 1; i <= 10; i++) {
                String id = String.format("G%03d", i);
                Guard guard = new Guard (
                    "guard" + i,
                    passwordEncoder.encode("Guard123"),
                    "Guard" + i,
                    id
                );
                guardRepository.save(guard);
            }
        }
    }
}

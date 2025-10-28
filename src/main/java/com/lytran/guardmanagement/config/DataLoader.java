package com.lytran.guardmanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void init() {
        Manager defaultManager = null;

        if(managerRepository.count() == 0) {
            defaultManager = new Manager(
                "manager1",
                passwordEncoder.encode("Manager123"),
                "Lý Trần Thu Ngân",
                "M001"
            );
            managerRepository.save(defaultManager);
        } else {
            defaultManager = managerRepository.findByUsername("manager1")
                .orElseThrow(() -> new RuntimeException("Default manager 'manager1' not found!"));
        }

        if(guardRepository.count() == 0 && defaultManager != null) {
            for(int i = 1; i <= 28; i++) {
                String id = String.format("G%03d", i);
                Guard guard = new Guard (
                    "guard" + i,
                    passwordEncoder.encode("Guard123"),
                    "Guard" + i,
                    id
                );
                if(i <= 14) {
                    guard.setTeam("A");
                } else {
                    guard.setTeam("B");
                }
                guard.setRotaGroup(((i - 1) % 7) + 1);

                guard.setManager(defaultManager);

                guardRepository.save(guard);
            }
        } else if (defaultManager == null) {
             System.err.println("DataLoader: Could not find or create a default manager. Guards will not be created.");
        }
    }
}

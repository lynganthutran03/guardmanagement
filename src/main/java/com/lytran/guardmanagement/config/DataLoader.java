package com.lytran.guardmanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.lytran.guardmanagement.entity.Role;
import com.lytran.guardmanagement.entity.User;
import com.lytran.guardmanagement.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if(userRepository.count() == 0) {
            User manager = new User(
                "manager1",
                passwordEncoder.encode("Manager123"),
                "Lý Trần Thu Ngân",
                "M001",
                Role.MANAGER
            );
            userRepository.save(manager);
            
            for(int i = 1; i <= 10; i++) {
                String id = String.format("G%03d", i);
                User guard = new User(
                    "guard" + i,
                    passwordEncoder.encode("Guard123"),
                    "Guard" + i,
                    id,
                    Role.GUARD
                );
                userRepository.save(guard);
            }
        }
    }
}

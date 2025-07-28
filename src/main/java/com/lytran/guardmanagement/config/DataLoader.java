package com.lytran.guardmanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.lytran.guardmanagement.entity.Employee;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.repository.EmployeeRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {
    @Autowired
    private EmployeeRepository employeeRepository;

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

        if(employeeRepository.count() == 0) {
            for(int i = 1; i <= 10; i++) {
                String id = String.format("G%03d", i);
                Employee guard = new Employee (
                    "guard" + i,
                    passwordEncoder.encode("Guard123"),
                    "Guard" + i,
                    id
                );
                employeeRepository.save(guard);
            }
        }
    }
}

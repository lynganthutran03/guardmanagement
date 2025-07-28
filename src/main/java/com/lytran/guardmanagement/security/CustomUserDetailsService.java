package com.lytran.guardmanagement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.repository.EmployeeRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find the user in employees first
        return employeeRepository.findByUsername(username)
                .<UserDetails>map(CustomUserDetails::new)
                .orElseGet(() ->
                        // If not found in guards, try managers
                        managerRepository.findByUsername(username)
                                .map(CustomUserDetails::new)
                                .orElseThrow(() ->
                                        new UsernameNotFoundException("User not found: " + username)
                                )
                );
    }
}
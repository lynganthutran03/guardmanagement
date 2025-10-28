package com.lytran.guardmanagement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find the user in guards first
        return guardRepository.findByUsername(username)
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
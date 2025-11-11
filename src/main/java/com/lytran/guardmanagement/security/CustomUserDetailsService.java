package com.lytran.guardmanagement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.repository.AdminRepository;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findByUsername(username)
                .<UserDetails>map(CustomUserDetails::new)
                .orElseGet(() ->
                        managerRepository.findByUsername(username)
                        .<UserDetails>map(CustomUserDetails::new)
                        .orElseGet(() ->
                                guardRepository.findByUsername(username)
                                .<UserDetails>map(CustomUserDetails::new)
                                .orElseThrow(()
                                        -> new UsernameNotFoundException("User not found: " + username)
                                )
                        )
                );
    }
}

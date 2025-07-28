package com.lytran.guardmanagement.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.lytran.guardmanagement.entity.Employee;
import com.lytran.guardmanagement.entity.Manager;

public class CustomUserDetails implements UserDetails {

    private Employee employee;
    private Manager manager;

    public CustomUserDetails(Employee employee) {
        this.employee = employee;
        this.manager = null;
    }

    public CustomUserDetails(Manager manager) {
        this.manager = manager;
        this.employee = null;
    }

    public boolean isEmployee() {
        return employee != null;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Manager getManager() {
        return manager;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = isEmployee() ? "ROLE_GUARD" : "ROLE_MANAGER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return isEmployee() ? employee.getUsername() : manager.getUsername();
    }

    @Override
    public String getPassword() {
        return isEmployee() ? employee.getPassword() : manager.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
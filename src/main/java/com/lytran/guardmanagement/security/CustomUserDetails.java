package com.lytran.guardmanagement.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.lytran.guardmanagement.entity.Admin;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Manager;

public class CustomUserDetails implements UserDetails {

    private Guard guard;
    private Manager manager;
    private Admin admin;

    public CustomUserDetails(Guard guard) {
        this.guard = guard;
        this.manager = null;
        this.admin = null;
    }

    public CustomUserDetails(Manager manager) {
        this.manager = manager;
        this.guard = null;
        this.admin = null;
    }

    public CustomUserDetails(Admin admin) {
        this.admin = admin;
        this.guard = null;
        this.manager = null;
    }

    public boolean isGuard() {
        return guard != null;
    }

    public Guard getGuard() {
        return guard;
    }

    public boolean isManager() {
        return manager != null;
    }

    public Manager getManager() {
        return manager;
    }

    public boolean isAdmin() {
        return admin != null;
    }

    public Admin getAdmin() {
        return admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role;
        if (isAdmin()) {
            role = "ROLE_ADMIN";
        } else if (isManager()) {
            role = "ROLE_MANAGER";
        } else {
            role = "ROLE_GUARD";
        }
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        if (isAdmin()) {
            return admin.getUsername();
        } else if (isManager()) {
            return manager.getUsername();
        } else {
            return guard.getUsername();
        }
    }

    @Override
    public String getPassword() {
        if (isAdmin()) {
            return admin.getPassword();
        } else if (isManager()) {
            return manager.getPassword();
        } else {
            return guard.getPassword();
        }
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

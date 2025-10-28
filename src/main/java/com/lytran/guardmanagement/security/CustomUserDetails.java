package com.lytran.guardmanagement.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Manager;

public class CustomUserDetails implements UserDetails {

    private Guard guard;
    private Manager manager;

    public CustomUserDetails(Guard guard) {
        this.guard = guard;
        this.manager = null;
    }

    public CustomUserDetails(Manager manager) {
        this.manager = manager;
        this.guard = null;
    }

    public boolean isGuard() {
        return guard != null;
    }

    public Guard getGuard() {
        return guard;
    }

    public Manager getManager() {
        return manager;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = isGuard() ? "ROLE_GUARD" : "ROLE_MANAGER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return isGuard() ? guard.getUsername() : manager.getUsername();
    }

    @Override
    public String getPassword() {
        return isGuard() ? guard.getPassword() : manager.getPassword();
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
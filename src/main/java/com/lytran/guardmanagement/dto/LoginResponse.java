package com.lytran.guardmanagement.dto;

import com.lytran.guardmanagement.entity.Role;

public class LoginResponse {
    private boolean success;
    private String message;
    private String fullName;
    private Role role;

    public LoginResponse(boolean success, String message, String fullName, Role role) {
        this.success = success;
        this.message = message;
        this.fullName = fullName;
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}

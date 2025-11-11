package com.lytran.guardmanagement.dto;

public class ManagerDTO {
    private Long id;
    private String username;
    private String fullName;
    private String identityNumber;
    private String role;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public String getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
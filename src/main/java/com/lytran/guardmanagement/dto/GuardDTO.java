package com.lytran.guardmanagement.dto;

public class GuardDTO {

    private Long id;
    private String username;
    private String fullName;
    private String identityNumber;
    private String team;
    private Integer rotaGroup;

    public GuardDTO() {
    }

    public GuardDTO(Long id, String username, String fullName, String identityNumber, String team, Integer rotaGroup) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.identityNumber = identityNumber;
        this.team = team;
        this.rotaGroup = rotaGroup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Integer getRotaGroup() {
        return rotaGroup;
    }

    public void setRotaGroup(Integer rotaGroup) {
        this.rotaGroup = rotaGroup;
    }
}
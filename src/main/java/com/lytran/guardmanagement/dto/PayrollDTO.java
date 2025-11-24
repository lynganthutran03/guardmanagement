package com.lytran.guardmanagement.dto;

public class PayrollDTO {
    private Long id;
    private String fullname;
    private String identityNumber;
    private int totalShifts;
    private double totalHours;
    private double totalSalary;
    private int lateCount;

    public PayrollDTO(Long id, String fullname, String identityNumber, int totalShifts, double totalHours,
            double totalSalary, int lateCount) {
        this.id = id;
        this.fullname = fullname;
        this.identityNumber = identityNumber;
        this.totalShifts = totalShifts;
        this.totalHours = totalHours;
        this.totalSalary = totalSalary;
        this.lateCount = lateCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public int getTotalShifts() {
        return totalShifts;
    }

    public void setTotalShifts(int totalShifts) {
        this.totalShifts = totalShifts;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public double getTotalSalary() {
        return totalSalary;
    }

    public void setTotalSalary(double totalSalary) {
        this.totalSalary = totalSalary;
    }

    public int getLateCount() {
        return lateCount;
    }

    public void setLateCount(int lateCount) {
        this.lateCount = lateCount;
    }
}
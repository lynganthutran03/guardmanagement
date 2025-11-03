package com.lytran.guardmanagement.dto;

import java.time.LocalDate;

import com.lytran.guardmanagement.entity.LeaveRequest;
import com.lytran.guardmanagement.model.LeaveStatus;

public class LeaveRequestResponseDTO {

    private Long id;
    private Long guardId;
    private String guardName;
    private String guardIdentityNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private String requestedAt;

    public LeaveRequestResponseDTO(LeaveRequest entity) {
        this.id = entity.getId();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.reason = entity.getReason();
        this.status = entity.getStatus();
        this.requestedAt = entity.getRequestedAt().toString();

        if (entity.getGuard() != null) {
            this.guardId = entity.getGuard().getId();
            this.guardName = entity.getGuard().getFullName();
            this.guardIdentityNumber = entity.getGuard().getIdentityNumber();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getGuardId() {
        return guardId;
    }

    public String getGuardName() {
        return guardName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getGuardIdentityNumber() {
        return guardIdentityNumber;
    }
}

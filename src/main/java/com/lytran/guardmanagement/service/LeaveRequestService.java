package com.lytran.guardmanagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lytran.guardmanagement.dto.LeaveRequestDTO;
import com.lytran.guardmanagement.dto.LeaveRequestResponseDTO;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.LeaveRequest;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.model.LeaveStatus;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.LeaveRequestRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;

@Service
public class LeaveRequestService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Transactional
    public LeaveRequestResponseDTO createLeaveRequest(LeaveRequestDTO requestDTO, String guardUsername) {
        Guard guard = guardRepository.findByUsername(guardUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảo vệ."));

        LocalDate endDate = requestDTO.getStartDate().plusDays(requestDTO.getDays() - 1);

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setGuard(guard);
        leaveRequest.setStartDate(requestDTO.getStartDate());
        leaveRequest.setEndDate(endDate);
        leaveRequest.setReason(requestDTO.getReason());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setRequestedAt(LocalDateTime.now());

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        return new LeaveRequestResponseDTO(savedRequest);
    }

    public List<LeaveRequestResponseDTO> getPendingRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findByStatusOrderByRequestedAtAsc(LeaveStatus.PENDING);
        
        return requests.stream()
                .map(LeaveRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestResponseDTO> getRequestHistoryForGuard(String guardUsername) {
        Guard guard = guardRepository.findByUsername(guardUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảo vệ."));
        
        List<LeaveRequest> requests = leaveRequestRepository.findByGuardIdOrderByRequestedAtDesc(guard.getId());
        
        return requests.stream()
                .map(LeaveRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestResponseDTO approveRequest(Long requestId, String managerUsername) {
        return updateRequestStatus(requestId, managerUsername, LeaveStatus.APPROVED);
    }

    @Transactional
    public LeaveRequestResponseDTO denyRequest(Long requestId, String managerUsername) {
        return updateRequestStatus(requestId, managerUsername, LeaveStatus.DENIED);
    }

    private LeaveRequestResponseDTO updateRequestStatus(Long requestId, String managerUsername, LeaveStatus newStatus) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn nghỉ phép."));

        Manager manager = managerRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quản lý."));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Đơn này đã được xử lý.");
        }

        request.setStatus(newStatus);
        request.setManager(manager);
        request.setReviewedAt(LocalDateTime.now());

        LeaveRequest updatedRequest = leaveRequestRepository.save(request);
        return new LeaveRequestResponseDTO(updatedRequest);
    }

    public List<LeaveRequestResponseDTO> getApprovedRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findByStatusOrderByRequestedAtAsc(LeaveStatus.APPROVED);
        
        return requests.stream()
                .map(LeaveRequestResponseDTO::new)
                .collect(Collectors.toList());
    }
}

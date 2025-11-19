package com.lytran.guardmanagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.ShiftHistory;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.ShiftHistoryRepository;
import com.lytran.guardmanagement.repository.ShiftRepository;

@Service
public class AttendanceService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private ShiftHistoryRepository shiftHistoryRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Value("#{'${app.attendance.allowed-ips}'.split(',')}")
    private List<String> allowedIpPrefixes;

    @Transactional
    public void checkIn(String username, String clientId) {
        boolean isAllowed = false;
        for (String prefix : allowedIpPrefixes) {
            if (clientId.trim().startsWith(prefix.trim())) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new RuntimeException("IP không hợp lệ (" + clientId + "). Vui lòng kết nối Wifi của trường.");
        }

        Guard guard = guardRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảo vệ."));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Shift> todayShifts = shiftRepository.findByGuardIdAndShiftDate(guard.getId(), today);
        Shift currentShift = null;
        
        for (Shift s : todayShifts) {
            LocalTime start = s.getTimeSlot().getStartTime();
            LocalTime end = s.getTimeSlot().getEndTime();
            
            if (now.isAfter(start.minusMinutes(30)) && now.isBefore(end)) {
                currentShift = s;
                break;
            }
        }

        if (currentShift == null) {
            throw new RuntimeException("Hiện tại không phải thời gian ca trực của bạn.");
        }

        if (shiftHistoryRepository.existsByShiftId(currentShift.getId())) {
            throw new RuntimeException("Bạn đã điểm danh cho ca này rồi.");
        }

        ShiftHistory history = new ShiftHistory();
        history.setShift(currentShift);
        history.setCompleteAt(LocalDateTime.now());

        if (now.isAfter(currentShift.getTimeSlot().getStartTime().plusMinutes(15))) {
            history.setAttendanceStatus("LATE");
        } else {
            history.setAttendanceStatus("PRESENT");
        }

        shiftHistoryRepository.save(history);
    }
}

package com.lytran.guardmanagement.config;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lytran.guardmanagement.entity.Admin;
import com.lytran.guardmanagement.entity.Location;
import com.lytran.guardmanagement.entity.TimeSlot;
import com.lytran.guardmanagement.repository.AdminRepository;
import com.lytran.guardmanagement.repository.LocationRepository;
import com.lytran.guardmanagement.repository.TimeSlotRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "Quản Trị Viên"
            );
            adminRepository.save(admin);
        }

        if (timeSlotRepository.count() == 0) {
            TimeSlot dayShift = new TimeSlot();
            dayShift.setName("DAY_SHIFT");
            dayShift.setStartTime(LocalTime.of(7, 30));
            dayShift.setEndTime(LocalTime.of(14, 30));
            timeSlotRepository.save(dayShift);

            TimeSlot nightShift = new TimeSlot();
            nightShift.setName("NIGHT_SHIFT");
            nightShift.setStartTime(LocalTime.of(14, 30));
            nightShift.setEndTime(LocalTime.of(21, 30));
            timeSlotRepository.save(nightShift);
        }

        if (locationRepository.count() == 0) {
            List<String> locations = List.of(
                    "Block 3", "Block 4", "Block 5", "Block 6", "Block 8",
                    "Block 10", "Block 11", "Gate 1", "Gate 2", "Gate 3"
            );
            for (String locName : locations) {
                Location loc = new Location();
                loc.setName(locName);
                locationRepository.save(loc);
            }
        }
    }
}

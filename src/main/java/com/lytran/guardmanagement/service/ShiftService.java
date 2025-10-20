package com.lytran.guardmanagement.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.dto.ShiftDTO;
import com.lytran.guardmanagement.dto.ShiftRequest;
import com.lytran.guardmanagement.entity.Employee;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.model.Location;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.model.TimeSlot;
import com.lytran.guardmanagement.repository.EmployeeRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;
import com.lytran.guardmanagement.repository.ShiftRepository;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;

    public ShiftService(ShiftRepository shiftRepository, EmployeeRepository employeeRepository,
            ManagerRepository managerRepository) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
        this.managerRepository = managerRepository;
    }

    private static final List<TimeSlot> TIME_SLOTS = Arrays.asList(TimeSlot.DAY_SHIFT, TimeSlot.NIGHT_SHIFT);
    private static final List<Location> LOCATIONS = Arrays.asList(
            Location.BLOCK_3, Location.BLOCK_4, Location.BLOCK_5,
            Location.BLOCK_6, Location.BLOCK_8, Location.BLOCK_10, Location.BLOCK_11,
            Location.GATE_1, Location.GATE_2, Location.GATE_3);

    private ShiftDTO convertToDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.setId(shift.getId());
        dto.setShiftDate(shift.getShiftDate());

        if (shift.getTimeSlot() != null) {
            dto.setTimeSlot(shift.getTimeSlot().name());
        }

        if (shift.getLocation() != null) {
            dto.setLocation(shift.getLocation().name());
        }

        if (shift.getGuard() != null) {
            dto.setEmployeeId(shift.getGuard().getId());
        }

        return dto;
    }

    /**
     * Manager-initiated shift generation logic
     */
    public Shift createShiftByManager(ShiftRequest request, String managerUsername) {
        if (request.getShiftDate() == null) {
            throw new IllegalArgumentException("Thiếu ngày làm việc.");
        }

        Manager manager = managerRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quản lý."));

        if (request.getShiftDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể tạo ca trực trong quá khứ.");
        }

        TimeSlot timeSlot = request.getTimeSlot();
        Location location = request.getLocation();

        if (timeSlot == null && location != null) {
            timeSlot = getRandomFreeTimeSlot(request.getShiftDate());
        } else if (timeSlot != null && location == null) {
            location = getRandomFreelocation(request.getShiftDate(), timeSlot);
        }

        if (timeSlot == null || location == null) {
            throw new IllegalArgumentException("Phải chọn ít nhất khung giờ hoặc khu vực.");
        }

        boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndLocation(request.getShiftDate(), timeSlot, location);
        if (taken) {
            throw new IllegalArgumentException("Ca trực này đã được chọn.");
        }

        Shift shift = new Shift();
        shift.setManager(manager);
        shift.setShiftDate(request.getShiftDate());
        shift.setTimeSlot(timeSlot);
        shift.setLocation(location);

        return shiftRepository.save(shift);
    }

    public List<Shift> getShiftsBeforeToday(Long employeeId) {
        return shiftRepository.findByGuardIdAndShiftDateBefore(employeeId, LocalDate.now());
    }

    public List<Shift> getAllShiftsForEmployee(Long employeeId) {
        return shiftRepository.findByGuardId(employeeId);
    }

    public Long getEmployeeIdByUsername(String username) {
        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."))
                .getId();
    }

    public TimeSlot getRandomFreeTimeSlot(LocalDate date) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            TimeSlot slot = TIME_SLOTS.get(random.nextInt(TIME_SLOTS.size()));
            boolean available = LOCATIONS.stream()
                    .anyMatch(location -> !shiftRepository.existsByShiftDateAndTimeSlotAndLocation(date, slot, location));
            if (available) {
                return slot;
            }
        }
        throw new RuntimeException("Không còn khung giờ trống.");
    }

    public Location getRandomFreelocation(LocalDate date, TimeSlot slot) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Location location = LOCATIONS.get(random.nextInt(LOCATIONS.size()));
            boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndLocation(date, slot, location);
            if (!taken) {
                return location;
            }
        }
        throw new RuntimeException("Không còn khu vực trống cho khung giờ đã chọn.");
    }

    public List<ShiftDTO> getShiftsForEmployeeByDate(Long employeeId, LocalDate date) {
        List<Shift> shifts = shiftRepository.findByGuardIdAndShiftDate(employeeId, date);
        return shifts.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ShiftDTO> getShiftHistory(Long employeeId) {
        return shiftRepository.findByGuardIdAndShiftDateBefore(employeeId, LocalDate.now()).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ShiftDTO> getAllShifts(Long employeeId) {
        return shiftRepository.findByGuardId(employeeId).stream()
                .filter(shift -> shift.getGuard() != null)
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<ShiftDTO> getTodayAcceptedShift(Long employeeId) {
        LocalDate today = LocalDate.now();
        List<Shift> shifts = shiftRepository.findByGuardIdAndShiftDate(employeeId, today);

        return shifts.stream()
                .filter(s -> s.getGuard() != null && s.getGuard().getId().equals(employeeId))
                .findFirst()
                .map(this::convertToDTO);
    }

    public void assignShiftToEmployee(Long shiftId, Long employeeId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca trực."));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        boolean alreadyAssigned = shiftRepository.existsByGuardIdAndShiftDate(employeeId, shift.getShiftDate());
        if (alreadyAssigned) {
            throw new RuntimeException("Nhân viên đã có ca trực vào ngày này.");
        }

        shift.setGuard(employee);
        shiftRepository.save(shift);
    }

}

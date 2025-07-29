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
import com.lytran.guardmanagement.model.Block;
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

    private static final List<TimeSlot> TIME_SLOTS = Arrays.asList(TimeSlot.MORNING, TimeSlot.AFTERNOON,
            TimeSlot.EVENING);
    private static final List<Block> BLOCKS = Arrays.asList(
            Block.BLOCK_3, Block.BLOCK_4, Block.BLOCK_5,
            Block.BLOCK_6, Block.BLOCK_8, Block.BLOCK_10, Block.BLOCK_11);

    private ShiftDTO convertToDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.setId(shift.getId());
        dto.setShiftDate(shift.getShiftDate());

        if (shift.getTimeSlot() != null) {
            dto.setTimeSlot(shift.getTimeSlot().name());
        }

        if (shift.getBlock() != null) {
            dto.setBlock(shift.getBlock().name());
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
        if (request.getEmployeeId() == null || request.getShiftDate() == null) {
            throw new IllegalArgumentException("Thiếu thông tin nhân viên hoặc ngày làm việc.");
        }

        Manager manager = managerRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quản lý."));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        if (request.getShiftDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể tạo ca trực trong quá khứ.");
        }

        TimeSlot timeSlot = request.getTimeSlot();
        Block block = request.getBlock();

        // Generate missing info if needed
        if (timeSlot == null && block != null) {
            timeSlot = getRandomFreeTimeSlot(request.getShiftDate());
        } else if (timeSlot != null && block == null) {
            block = getRandomFreeBlock(request.getShiftDate(), timeSlot);
        }

        if (timeSlot == null || block == null) {
            throw new IllegalArgumentException("Phải chọn ít nhất khung giờ hoặc block.");
        }

        boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndBlock(request.getShiftDate(), timeSlot, block);
        if (taken) {
            throw new IllegalArgumentException("Ca trực này đã được chọn.");
        }

        Shift shift = new Shift();
        shift.setGuard(employee);
        shift.setManager(manager);
        shift.setShiftDate(request.getShiftDate());
        shift.setTimeSlot(timeSlot);
        shift.setBlock(block);

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
            boolean available = BLOCKS.stream()
                    .anyMatch(block -> !shiftRepository.existsByShiftDateAndTimeSlotAndBlock(date, slot, block));
            if (available) {
                return slot;
            }
        }
        throw new RuntimeException("Không còn khung giờ trống.");
    }

    public Block getRandomFreeBlock(LocalDate date, TimeSlot slot) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Block block = BLOCKS.get(random.nextInt(BLOCKS.size()));
            boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndBlock(date, slot, block);
            if (!taken) {
                return block;
            }
        }
        throw new RuntimeException("Không còn block trống cho khung giờ đã chọn.");
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

        if (shifts.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertToDTO(shifts.get(0)));
    }

    public void assignShiftToEmployee(Long shiftId, Long employeeId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca trực."));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        shift.setGuard(employee);
        shiftRepository.save(shift);
    }
}

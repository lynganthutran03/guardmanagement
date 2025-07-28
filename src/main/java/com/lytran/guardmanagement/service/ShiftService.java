package com.lytran.guardmanagement.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.entity.Employee;
import com.lytran.guardmanagement.model.Block;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.model.TimeSlot;
import com.lytran.guardmanagement.repository.EmployeeRepository;
import com.lytran.guardmanagement.repository.ShiftRepository;

@Service
public class ShiftService {

    public final ShiftRepository shiftRepository;
    private final EmployeeRepository guardRepository;

    public ShiftService(ShiftRepository shiftRepository, EmployeeRepository guardRepository) {
        this.shiftRepository = shiftRepository;
        this.guardRepository = guardRepository;
    }

    private static final List<TimeSlot> TIME_SLOTS = Arrays.asList(TimeSlot.MORNING, TimeSlot.AFTERNOON, TimeSlot.EVENING);
    private static final List<Block> BLOCKS = Arrays.asList(
            Block.BLOCK_3, Block.BLOCK_4, Block.BLOCK_5,
            Block.BLOCK_6, Block.BLOCK_8, Block.BLOCK_10, Block.BLOCK_11);

    // Manager creates shift for guard
    public Shift createShift(Long guardId, TimeSlot timeSlot, Block block, LocalDate date) {
        Employee guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new RuntimeException("Guard not found"));

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Không thể tạo ca trực trong quá khứ.");
        }

        boolean isTaken = shiftRepository.existsByShiftDateAndTimeSlotAndBlock(date, timeSlot, block);
        if (isTaken) {
            throw new IllegalStateException("Ca trực này đã có người đảm nhận.");
        }

        Shift shift = new Shift();
        shift.setGuard(guard);
        shift.setShiftDate(date);
        shift.setTimeSlot(timeSlot);
        shift.setBlock(block);

        return shiftRepository.save(shift);
    }

    public List<Shift> getShiftsForGuardByDate(Long guardId, LocalDate date) {
        return shiftRepository.findByGuardIdAndShiftDate(guardId, date);
    }

    public List<Shift> getShiftsBeforeToday(Long guardId) {
        return shiftRepository.findByGuardIdAndShiftDateBefore(guardId, LocalDate.now());
    }

    public List<Shift> getAllShiftsForGuard(Long guardId) {
        return shiftRepository.findByGuardId(guardId);
    }

    public Long getGuardIdByUsername(String username) {
        return guardRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Guard not found"))
                .getId();
    }

    // Optional random generator helper
    public TimeSlot getRandomFreeTimeSlot(LocalDate date) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            TimeSlot slot = TIME_SLOTS.get(random.nextInt(TIME_SLOTS.size()));
            boolean available = BLOCKS.stream().anyMatch(block ->
                    !shiftRepository.existsByShiftDateAndTimeSlotAndBlock(date, slot, block));
            if (available) return slot;
        }
        throw new RuntimeException("No available time slot");
    }

    public Block getRandomFreeBlock(LocalDate date, TimeSlot slot) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Block block = BLOCKS.get(random.nextInt(BLOCKS.size()));
            boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndBlock(date, slot, block);
            if (!taken) return block;
        }
        throw new RuntimeException("No available block for selected slot");
    }
}

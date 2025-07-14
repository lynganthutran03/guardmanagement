package com.lytran.guardmanagement.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.entity.User;
import com.lytran.guardmanagement.model.Block;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.model.TimeSlot;
import com.lytran.guardmanagement.repository.ShiftRepository;
import com.lytran.guardmanagement.repository.UserRepository;

@Service
public class ShiftService {

    public final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public ShiftService(ShiftRepository shiftRepository, UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    private static final List<TimeSlot> TIME_SLOTS = Arrays.asList(TimeSlot.MORNING, TimeSlot.AFTERNOON, TimeSlot.EVENING);
    private static final List<Block> BLOCKS = Arrays.asList(Block.BLOCK_3, Block.BLOCK_4, Block.BLOCK_5, Block.BLOCK_6, Block.BLOCK_8, Block.BLOCK_10, Block.BLOCK_11);

    public Shift generateRandomShift(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate today = LocalDate.now();
        int attempts = 0;

        while (attempts < 20) {
            TimeSlot randomSlot = TIME_SLOTS.get(new Random().nextInt(TIME_SLOTS.size()));
            Block randomBlock = BLOCKS.get(new Random().nextInt(BLOCKS.size()));

            boolean isTaken = shiftRepository.existsByShiftDateAndTimeSlotAndBlock(today, randomSlot, randomBlock);

            if (!isTaken) {
                Shift shift = new Shift();
                shift.setUser(user);
                shift.setShiftDate(today);
                shift.setTimeSlot(randomSlot);
                shift.setBlock(randomBlock);
                shift.setAccepted(false);

                return shiftRepository.save(shift);
            }

            attempts++;
        }

        throw new RuntimeException("Could not find an available shift after 20 attempts");
    }

    public Shift generateShiftForUser(Long userId,
            TimeSlot chosenTime,
            Block chosenBlock) {

        User user = userRepository.findById(userId)
             .orElseThrow(() -> new RuntimeException("User not found"));

        TimeSlot time = chosenTime != null
                ? chosenTime
                : randomFreeTimeSlotForDate(LocalDate.now(), userId);

        Block block = chosenBlock != null
                ? chosenBlock
                : randomFreeBlockForDateAndTime(LocalDate.now(), time);

        Shift shift = new Shift();
        shift.setUser(user);
        shift.setShiftDate(LocalDate.now());
        shift.setTimeSlot(time);
        shift.setBlock(block);
        shift.setAccepted(false);
        shiftRepository.save(shift);
        return shiftRepository.save(shift);
    }

    public List<Shift> getGeneratedShiftsForUser(Long userId, LocalDate date) {
        return shiftRepository.findByUserIdAndShiftDate(userId, date);
    }

    public void acceptShift(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        shift.setAccepted(true);
        shiftRepository.save(shift);
    }

    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private TimeSlot randomFreeTimeSlotForDate(LocalDate date, Long userId) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            TimeSlot ts = TIME_SLOTS.get(random.nextInt(TIME_SLOTS.size()));
            boolean available = BLOCKS.stream().anyMatch(block
                    -> !shiftRepository.existsByShiftDateAndTimeSlotAndBlock(date, ts, block));
            if (available) {
                return ts;
            }
        }
        throw new RuntimeException("No available time slot found");
    }

    private Block randomFreeBlockForDateAndTime(LocalDate date, TimeSlot timeSlot) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Block block = BLOCKS.get(random.nextInt(BLOCKS.size()));
            boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndBlock(date, timeSlot, block);
            if (!taken) {
                return block;
            }
        }
        throw new RuntimeException("No available block for selected time slot");
    }
}

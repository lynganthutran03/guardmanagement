package com.lytran.guardmanagement.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.lytran.guardmanagement.dto.ShiftDTO;
import com.lytran.guardmanagement.dto.ShiftRequest;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.model.Location;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.model.TimeSlot;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;
import com.lytran.guardmanagement.repository.ShiftRepository;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final GuardRepository guardRepository;
    private final ManagerRepository managerRepository;

    public ShiftService(ShiftRepository shiftRepository, GuardRepository guardRepository,
            ManagerRepository managerRepository) {
        this.shiftRepository = shiftRepository;
        this.guardRepository = guardRepository;
        this.managerRepository = managerRepository;
    }

    private static final LocalDate ROTATION_ANCHOR_DATE = LocalDate.of(2024, 1, 1);

    private static final List<TimeSlot> TIME_SLOTS = Arrays.asList(TimeSlot.DAY_SHIFT, TimeSlot.NIGHT_SHIFT);
    private static final List<Location> ALL_LOCATIONS = Arrays.asList(Location.values());
    private static final int NUM_LOCATIONS = ALL_LOCATIONS.size();

    public void generateWeekForGuard(long guardId, LocalDate weekStartDate, String managerUsername) {
        if (weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("Ngày đầu tiên trong tuần phải là thứ 2.");
        }

        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        Manager manager = managerRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quản lý."));

        if (guard.getTeam() == null || guard.getRotaGroup() == null) {
            throw new IllegalArgumentException("Nhân viên này chưa được gán Đội hoặc Nhóm Rota.");
        }

        LocalDate weekEndDate = weekStartDate.plusDays(6);
        boolean alreadyAssigned = shiftRepository.existsByGuardIdAndShiftDateBetween(guardId, weekStartDate, weekEndDate);
        if (alreadyAssigned) {
            throw new RuntimeException("Nhân viên đã có lịch làm việc trong tuần này.");
        }

        TimeSlot weekTimeSlot = getRotationTimeSlotForTeam(guard.getTeam(), weekStartDate);

        List<Shift> newShifts = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate shiftDate = weekStartDate.plusDays(i);

            if (isDayOff(guard.getRotaGroup(), shiftDate.getDayOfWeek())) {
                continue;
            }

            Shift shift = new Shift();
            shift.setGuard(guard);
            shift.setManager(manager);
            shift.setShiftDate(shiftDate);

            Location dailyLocation = findLocationForGuardOnDate(guard, shiftDate, weekTimeSlot);
            shift.setLocation(dailyLocation);

            shift.setTimeSlot(weekTimeSlot);
            newShifts.add(shift);
        }

        shiftRepository.saveAll(newShifts);
    }

    private boolean isDayOff(int rotaGroup, DayOfWeek day) {
        switch (rotaGroup) {
            case 1: // Group 1 is off Mon, Tue
                return day == DayOfWeek.MONDAY || day == DayOfWeek.TUESDAY;
            case 2: // Group 2 is off Wed, Thu
                return day == DayOfWeek.WEDNESDAY || day == DayOfWeek.THURSDAY;
            case 3: // Group 3 is off Thu, Fri
                return day == DayOfWeek.THURSDAY || day == DayOfWeek.FRIDAY;
            case 4: // Group 4 is off Fri, Sat
                return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY;
            case 5: // Group 5 is off Sat, Sun
                return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
            case 6: // Group 6 is off Sun, Mon
                return day == DayOfWeek.SUNDAY || day == DayOfWeek.MONDAY;
            case 7: // Group 7 is off Tue, Wed (Staggered to balance the week)
                return day == DayOfWeek.TUESDAY || day == DayOfWeek.WEDNESDAY;
            default:
                return false;
        }
    }

    private TimeSlot getRotationTimeSlotForTeam(String team, LocalDate date) {
        long daysSinceAnchor = java.time.temporal.ChronoUnit.DAYS.between(ROTATION_ANCHOR_DATE, date);
        int weekNumber = (int) (daysSinceAnchor / 7);
        int cyclePosition = weekNumber % 4;
        
        boolean isTeamADay = (cyclePosition == 0 || cyclePosition == 1);

        if (team.equals("A")) {
            return isTeamADay ? TimeSlot.DAY_SHIFT : TimeSlot.NIGHT_SHIFT;
        } else {
            return isTeamADay ? TimeSlot.NIGHT_SHIFT : TimeSlot.DAY_SHIFT;
        }
    }

    private Location findLocationForGuardOnDate(Guard guard, LocalDate date, TimeSlot timeSlot) {
        int dayIndex = date.getDayOfWeek().getValue() -1;
        int guardOffset = guard.getRotaGroup() -1;
        
        for (int i = 0; i < NUM_LOCATIONS; i++) {
            int locationIndex = (dayIndex + guardOffset + i) % NUM_LOCATIONS;
            Location potentialLocation = ALL_LOCATIONS.get(locationIndex);

            boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndLocation(date, timeSlot, potentialLocation);
            
            if (!taken) {
                return potentialLocation;
            }
        }

        throw new RuntimeException("Không còn khu vực trống cho: " + date + " " + timeSlot);
    }

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
            dto.setGuardId(shift.getGuard().getId());
        }

        return dto;
    }

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

    public List<Shift> getShiftsBeforeToday(Long guardId) {
        return shiftRepository.findByGuardIdAndShiftDateBefore(guardId, LocalDate.now());
    }

    public List<Shift> getAllShiftsForGuard(Long guardId) {
        return shiftRepository.findByGuardId(guardId);
    }

    public Long getGuardIdByUsername(String username) {
        return guardRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."))
                .getId();
    }

    public TimeSlot getRandomFreeTimeSlot(LocalDate date) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            TimeSlot slot = TIME_SLOTS.get(random.nextInt(TIME_SLOTS.size()));
            boolean available = ALL_LOCATIONS.stream()
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
            Location location = ALL_LOCATIONS.get(random.nextInt(ALL_LOCATIONS.size()));
            boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndLocation(date, slot, location);
            if (!taken) {
                return location;
            }
        }
        throw new RuntimeException("Không còn khu vực trống cho khung giờ đã chọn.");
    }

    public List<ShiftDTO> getShiftsForGuardByDate(Long guardId, LocalDate date) {
        List<Shift> shifts = shiftRepository.findByGuardIdAndShiftDate(guardId, date);
        return shifts.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ShiftDTO> getShiftHistory(Long guardId) {
        return shiftRepository.findByGuardIdAndShiftDateBefore(guardId, LocalDate.now()).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ShiftDTO> getAllShifts(Long guardId) {
        return shiftRepository.findByGuardId(guardId).stream()
                .filter(shift -> shift.getGuard() != null)
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<ShiftDTO> getTodayAcceptedShift(Long guardId) {
        LocalDate today = LocalDate.now();
        List<Shift> shifts = shiftRepository.findByGuardIdAndShiftDate(guardId, today);

        return shifts.stream()
                .filter(s -> s.getGuard() != null && s.getGuard().getId().equals(guardId))
                .findFirst()
                .map(this::convertToDTO);
    }

    public void assignShiftToGuard(Long shiftId, Long guardId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca trực."));

        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        boolean alreadyAssigned = shiftRepository.existsByGuardIdAndShiftDate(guardId, shift.getShiftDate());
        if (alreadyAssigned) {
            throw new RuntimeException("Nhân viên đã có ca trực vào ngày này.");
        }

        shift.setGuard(guard);
        shiftRepository.save(shift);
    }
}

package com.lytran.guardmanagement.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lytran.guardmanagement.dto.GuardDTO;
import com.lytran.guardmanagement.dto.ShiftDTO;
import com.lytran.guardmanagement.dto.ShiftRequest;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.model.LeaveStatus;
import com.lytran.guardmanagement.model.Location;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.model.TimeSlot;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.LeaveRequestRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;
import com.lytran.guardmanagement.repository.ShiftRepository;

@Service
public class ShiftService {

    @Autowired
    private final ShiftRepository shiftRepository;

    @Autowired
    private final GuardRepository guardRepository;

    @Autowired
    private final ManagerRepository managerRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

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

    @Transactional
    public void generateWeekForTeam(String team, LocalDate weekStartDate, String managerUsername) {
        if (!team.equals("A") && !team.equals("B")) {
            throw new IllegalArgumentException("Invalid team specified. Must be 'A' or 'B'.");
        }
        if (weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("Week start date must be a Monday.");
        }

        List<Guard> guardsInTeam = guardRepository.findByTeam(team);
        if (guardsInTeam.isEmpty()) {
            System.out.println("No guards found for team " + team + ". No schedules generated.");
            return;
        }

        Manager manager = managerRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quản lý."));

        List<String> errors = new ArrayList<>();
        for (Guard guard : guardsInTeam) {
            try {
                generateWeekForGuard(guard.getId(), weekStartDate, managerUsername);
            } catch (RuntimeException e) {
                String errorMsg = "Error generating for guard ID " + guard.getId() + " (" + guard.getUsername() + "): " + e.getMessage();
                System.err.println(errorMsg);
                errors.add(errorMsg);
            }
        }

        if (!errors.isEmpty()) {
            System.err.println("Completed generation for team " + team + " with " + errors.size() + " errors.");
        }
    }

    public void generateWeekForGuard(long guardId, LocalDate weekStartDate, String managerUsername) {
        if (weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("Ngày đầu tiên trong tuần phải là thứ 2.");
        }

        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        Manager manager = managerRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quản lý."));

        if (guard.getTeam() == null || guard.getRotaGroup() == null) {
            throw new IllegalArgumentException("Nhân viên này chưa được gán đội hoặc nhóm xoay ca.");
        }

        LocalDate weekEndDate = weekStartDate.plusDays(6);
        boolean alreadyAssigned = shiftRepository.existsByGuardIdAndShiftDateBetween(guardId, weekStartDate, weekEndDate);
        if (alreadyAssigned) {
            System.out.println("Nhân viên đã có ca trực trong tuần này. Bỏ qua tạo mới.");
            return;
        }

        TimeSlot weekTimeSlot = getRotationTimeSlotForTeam(guard.getTeam(), weekStartDate);

        List<Shift> newShifts = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate shiftDate = weekStartDate.plusDays(i);

            if (isSundayOffForRotation(guard, shiftDate)) {
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

    private boolean isSundayOffForRotation(Guard guard, LocalDate date) {
        if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            return false;
        }

        long daysSinceAnchor = java.time.temporal.ChronoUnit.DAYS.between(ROTATION_ANCHOR_DATE, date);
        int weekNumber = (int) (daysSinceAnchor / 7);
        int cyclePosition = weekNumber % 4;

        int guardGroup = guard.getRotaGroup();
        if (cyclePosition == 1 && (guardGroup == 1 || guardGroup == 2)) {
            return true;
        } else if (cyclePosition == 3 && (guardGroup == 3 || guardGroup == 4)) {
            return true;
        } else if (cyclePosition == 0 && (guardGroup == 5 || guardGroup == 6)) {
            return true;
        } else if (cyclePosition == 2 && (guardGroup == 7)) {
            return true;
        } else {
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
        int dayIndex = date.getDayOfWeek().getValue() - 1;
        int guardOffset = guard.getRotaGroup() - 1;

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
            dto.setGuardName(shift.getGuard().getFullName());
            dto.setGuardIdentityNumber(shift.getGuard().getIdentityNumber());
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

    public List<ShiftDTO> getAllShiftsForManager() {
        LocalDate today = LocalDate.now();
        List<Shift> shifts = shiftRepository.findByGuardIsNotNullAndShiftDateBeforeOrderByShiftDateDesc(today);
        return shifts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ShiftDTO> getShiftsForGuardInRange(Long guardId, LocalDate startDate, LocalDate endDate) {
        List<Shift> shifts = shiftRepository.findAllByGuardIdAndShiftDateBetween(guardId, startDate, endDate);
        
        return shifts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<GuardDTO> getAvailableGuardsForShift(LocalDate shiftDate) {
        List<Guard> allGuards = guardRepository.findAll();

        List<GuardDTO> availableGuards = allGuards.stream()
                .filter(guard -> {
                    boolean hasShift = shiftRepository.existsByGuardIdAndShiftDate(guard.getId(), shiftDate);

                    boolean onLeave = leaveRequestRepository.existsByGuardIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            guard.getId(),
                            LeaveStatus.APPROVED,
                            shiftDate,
                            shiftDate
                    );

                    return !hasShift && !onLeave;
                })
                .map(guard -> {
                    GuardDTO dto = new GuardDTO();
                    dto.setId(guard.getId());
                    dto.setFullName(guard.getFullName());
                    dto.setIdentityNumber(guard.getIdentityNumber());
                    dto.setTeam(guard.getTeam());
                    dto.setRotaGroup(guard.getRotaGroup());
                    return dto;
                })
                .collect(Collectors.toList());

        return availableGuards;
    }
}

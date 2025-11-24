package com.lytran.guardmanagement.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lytran.guardmanagement.dto.GuardDTO;
import com.lytran.guardmanagement.dto.ShiftDTO;
import com.lytran.guardmanagement.dto.ShiftRequest;
import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Location;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.entity.ShiftHistory;
import com.lytran.guardmanagement.entity.TimeSlot;
import com.lytran.guardmanagement.model.LeaveStatus;
import com.lytran.guardmanagement.model.Shift;
import com.lytran.guardmanagement.repository.GuardRepository;
import com.lytran.guardmanagement.repository.LeaveRequestRepository;
import com.lytran.guardmanagement.repository.LocationRepository;
import com.lytran.guardmanagement.repository.ManagerRepository;
import com.lytran.guardmanagement.repository.ShiftHistoryRepository;
import com.lytran.guardmanagement.repository.ShiftRepository;
import com.lytran.guardmanagement.repository.TimeSlotRepository;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ShiftHistoryRepository shiftHistoryRepository;

    private static final LocalDate ROTATION_ANCHOR_DATE = LocalDate.of(2024, 1, 1);

    @Transactional
    public void generateWeekForTeam(String team, LocalDate weekStartDate, String managerUsername) {
        if (!team.equals("A") && !team.equals("B")) {
            throw new IllegalArgumentException("Lỗi team (phải là team A hoặc team B)");
        }

        if (weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("Hãy chọn thứ 2 trong tuần");
        }

        LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        if (weekStartDate.isBefore(currentWeekStart)) {
            throw new IllegalArgumentException("Không thể tạo lịch cho tuần trong quá khứ.");
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
        List<Location> allLocations = locationRepository.findAll();
        int numLocations = allLocations.size();
        if (numLocations == 0) {
            throw new RuntimeException("Không có Locations trong DB!");
        }

        for (int i = 0; i < 7; i++) {
            LocalDate shiftDate = weekStartDate.plusDays(i);

            Shift shift = new Shift();
            shift.setGuard(guard);
            shift.setManager(manager);
            shift.setShiftDate(shiftDate);
            Location dailyLocation = findLocationForGuardOnDate(guard, shiftDate, weekTimeSlot, allLocations, numLocations);
            shift.setLocation(dailyLocation);
            shift.setTimeSlot(weekTimeSlot);
            newShifts.add(shift);
        }
        shiftRepository.saveAll(newShifts);
    }

    private TimeSlot getRotationTimeSlotForTeam(String team, LocalDate date) {
        long daysSinceAnchor = java.time.temporal.ChronoUnit.DAYS.between(ROTATION_ANCHOR_DATE, date);
        int weekNumber = (int) (daysSinceAnchor / 7);
        int cyclePosition = weekNumber % 4;
        boolean isTeamADay = (cyclePosition == 0 || cyclePosition == 1);
        String timeSlotName;
        if (team.equals("A")) {
            timeSlotName = isTeamADay ? "DAY_SHIFT" : "NIGHT_SHIFT";
        } else {
            timeSlotName = isTeamADay ? "NIGHT_SHIFT" : "DAY_SHIFT";
        }
        return timeSlotRepository.findByName(timeSlotName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy TimeSlot với tên: " + timeSlotName));
    }

    private Location findLocationForGuardOnDate(Guard guard, LocalDate date, TimeSlot timeSlot, List<Location> allLocations, int numLocations) {
        int dayIndex = date.getDayOfWeek().getValue() - 1;
        int guardOffset = guard.getRotaGroup() - 1;
        for (int i = 0; i < numLocations; i++) {
            int locationIndex = (dayIndex + guardOffset + i) % numLocations;
            Location potentialLocation = allLocations.get(locationIndex);
            boolean taken = shiftRepository.existsByShiftDateAndTimeSlotAndLocation(date, timeSlot, potentialLocation);
            if (!taken) {
                return potentialLocation;
            }
        }
        throw new RuntimeException("Không còn khu vực trống cho: " + date + " " + timeSlot.getName());
    }

    private ShiftDTO convertToDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.setId(shift.getId());
        dto.setShiftDate(shift.getShiftDate());
        if (shift.getTimeSlot() != null) {
            dto.setTimeSlot(shift.getTimeSlot().getName());
        }
        if (shift.getLocation() != null) {
            dto.setLocation(shift.getLocation().getName());
        }
        if (shift.getGuard() != null) {
            dto.setGuardId(shift.getGuard().getId());
            dto.setGuardName(shift.getGuard().getFullName());
            dto.setGuardIdentityNumber(shift.getGuard().getIdentityNumber());
        }
        return dto;
    }

    public Shift createShiftByManager(ShiftRequest request, String managerUsername) {
        throw new UnsupportedOperationException("Tạo ca lẻ (createShiftByManager) không còn được hỗ trợ sau khi chuyển sang Entity.");
    }

    public Long getGuardIdByUsername(String username) {
        return guardRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."))
                .getId();
    }

    public List<ShiftDTO> getShiftsForGuardByDate(Long guardId, LocalDate date) {
        List<Shift> shifts = shiftRepository.findByGuardIdAndShiftDate(guardId, date);
        return shifts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ShiftDTO> getShiftHistory(Long guardId) {
        List<Shift> shifts = shiftRepository.findByGuardIdAndShiftDateBefore(guardId, LocalDate.now());

        return shifts.stream()
                .map(shift -> {
                    ShiftDTO dto = convertToDTO(shift);

                    if (shift.getTimeSlot() != null) {
                        dto.setStartTime(shift.getTimeSlot().getStartTime());
                        dto.setEndTime(shift.getTimeSlot().getEndTime());
                    }

                    Optional<ShiftHistory> historyOpt = shiftHistoryRepository.findByShiftId(shift.getId());

                    if (historyOpt.isPresent()) {
                        ShiftHistory history = historyOpt.get();
                        dto.setAttendanceStatus(history.getAttendanceStatus());
                        dto.setCheckInTime(history.getCompleteAt());
                    } else {
                        dto.setAttendanceStatus("ABSENT");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ShiftDTO> getAllShifts(Long guardId) {
        return shiftRepository.findByGuardId(guardId).stream()
                .filter(shift -> shift.getGuard() != null)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

        boolean hasOtherShift = shiftRepository.existsByGuardIdAndShiftDate(guardId, shift.getShiftDate());
        boolean onLeave = leaveRequestRepository.existsByGuardIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                guardId, LeaveStatus.APPROVED, shift.getShiftDate(), shift.getShiftDate()
        );
        if (hasOtherShift || onLeave) {
            throw new RuntimeException("Nhân viên đã có ca trực hoặc đang nghỉ phép vào ngày này.");
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

    public boolean checkScheduleExists(String team, LocalDate start, LocalDate end) {
        return shiftRepository.existsByTeamAndDateRange(team, start, end);
    }
}

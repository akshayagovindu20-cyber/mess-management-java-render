package com.campusmess.controller;

import com.campusmess.entity.MealAttendance;
import com.campusmess.entity.MealSchedule;
import com.campusmess.entity.User;
import com.campusmess.repository.MealAttendanceRepository;
import com.campusmess.repository.MealScheduleRepository;
import com.campusmess.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceApiController {

    private final MealAttendanceRepository attendanceRepository;
    private final MealScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public AttendanceApiController(MealAttendanceRepository attendanceRepository,
                                  MealScheduleRepository scheduleRepository,
                                  UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> markAttendance(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("studentId") || !request.containsKey("date") || !request.containsKey("mealType")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Student ID, date, and meal type are required."));
        }

        Long studentId = Long.valueOf(request.get("studentId").toString());
        String dateStr = request.get("date").toString().trim();
        String mealType = request.get("mealType").toString().trim().toUpperCase();
        String status = request.getOrDefault("status", "PRESENT").toString().trim().toUpperCase();

        Optional<User> studentOpt = userRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Student not found."));
        }
        User student = studentOpt.get();

        LocalDate reqDate = LocalDate.parse(dateStr);
        LocalDate today = LocalDate.now();

        // Rule 1: Deadline validation
        if (reqDate.isBefore(today)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Cannot mark attendance for a past date."));
        }

        Optional<MealSchedule> scheduleOpt = scheduleRepository.findByMealTypeIgnoreCase(mealType);
        if (scheduleOpt.isPresent() && reqDate.equals(today)) {
            MealSchedule schedule = scheduleOpt.get();
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
                LocalTime cutoff = LocalTime.parse(schedule.getAttendanceDeadline().trim(), dtf);
                if (LocalTime.now().isAfter(cutoff)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Attendance deadline has passed for " + mealType + " today. Cutoff was " + schedule.getAttendanceDeadline() + "."
                    ));
                }
            } catch (Exception ignored) {
                // If deadline format differs, continue gracefully
            }
        }

        // Rule 2: Duplicate prevention
        Optional<MealAttendance> existing = attendanceRepository.findByStudentIdAndDateAndMealTypeIgnoreCase(studentId, dateStr, mealType);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Attendance has already been marked as " + existing.get().getStatus() + " for " + mealType + " on " + dateStr + "."
            ));
        }

        MealAttendance attendance = new MealAttendance(student, dateStr, mealType, status);
        MealAttendance saved = attendanceRepository.save(attendance);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Attendance marked successfully as " + status + ".");
        res.put("attendance", toAttendanceMap(saved));

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAttendance(@RequestParam Long studentId) {
        List<MealAttendance> list = attendanceRepository.findByStudentIdOrderByDateDesc(studentId);

        long total = list.size();
        long attended = list.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
        long missed = list.stream().filter(a -> "ABSENT".equalsIgnoreCase(a.getStatus())).count();
        double percentage = total > 0 ? ((double) attended / total) * 100.0 : 0.0;

        List<Map<String, Object>> mapped = list.stream().map(this::toAttendanceMap).toList();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMeals", total);
        summary.put("attendedMeals", attended);
        summary.put("missedMeals", missed);
        summary.put("attendancePercentage", Math.round(percentage * 10.0) / 10.0);

        Map<String, Object> response = new HashMap<>();
        response.put("attendances", mapped);
        response.put("summary", summary);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/expected")
    public ResponseEntity<?> getExpectedServings(@RequestParam(required = false) String date) {
        String queryDate = (date != null && !date.trim().isEmpty()) ? date.trim() : LocalDate.now().toString();

        long totalStudents = userRepository.findByRoleIgnoreCase("STUDENT").size();
        if (totalStudents == 0) totalStudents = 1; // Avoid division by zero

        List<MealAttendance> attendances = attendanceRepository.findByDate(queryDate);

        Map<String, Object> result = new HashMap<>();
        result.put("date", queryDate);
        result.put("totalStudents", totalStudents);

        String[] mealTypes = {"BREAKFAST", "LUNCH", "DINNER"};
        Map<String, Object> mealStats = new HashMap<>();

        for (String m : mealTypes) {
            long present = attendances.stream()
                    .filter(a -> m.equalsIgnoreCase(a.getMealType()) && "PRESENT".equalsIgnoreCase(a.getStatus()))
                    .count();
            long absent = attendances.stream()
                    .filter(a -> m.equalsIgnoreCase(a.getMealType()) && "ABSENT".equalsIgnoreCase(a.getStatus()))
                    .count();
            long expected = present > 0 ? present : Math.max(1, (long)(totalStudents * 0.85)); // realistic fallback if early morning

            Map<String, Object> stat = new HashMap<>();
            stat.put("present", present);
            stat.put("absent", absent);
            stat.put("unmarked", Math.max(0, totalStudents - present - absent));
            stat.put("expectedServings", expected);

            mealStats.put(m.toLowerCase(), stat);
        }

        result.put("meals", mealStats);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<?> getAllAttendance(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) Long studentId) {

        List<MealAttendance> list;
        if (studentId != null) {
            list = attendanceRepository.findByStudentIdOrderByDateDesc(studentId);
        } else if (date != null && mealType != null) {
            list = attendanceRepository.findByDateAndMealTypeIgnoreCase(date.trim(), mealType.trim());
        } else if (date != null) {
            list = attendanceRepository.findByDate(date.trim());
        } else {
            list = attendanceRepository.findAll();
        }

        List<Map<String, Object>> mapped = list.stream().map(this::toAttendanceMap).toList();
        return ResponseEntity.ok(mapped);
    }

    private Map<String, Object> toAttendanceMap(MealAttendance a) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", a.getId());
        map.put("date", a.getDate());
        map.put("mealType", a.getMealType());
        map.put("status", a.getStatus());
        map.put("markedAt", a.getMarkedAt());
        if (a.getStudent() != null) {
            map.put("studentId", a.getStudent().getId());
            map.put("studentName", a.getStudent().getName());
            map.put("email", a.getStudent().getEmail());
            if (a.getStudent().getProfile() != null) {
                map.put("rollNo", a.getStudent().getProfile().getStudentId());
                map.put("hostelBlock", a.getStudent().getProfile().getHostelBlock());
                map.put("roomNumber", a.getStudent().getProfile().getRoomNumber());
            }
        }
        return map;
    }
}

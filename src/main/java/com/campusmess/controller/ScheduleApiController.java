package com.campusmess.controller;

import com.campusmess.entity.MealSchedule;
import com.campusmess.repository.MealScheduleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class ScheduleApiController {

    private final MealScheduleRepository scheduleRepository;

    public ScheduleApiController(MealScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @GetMapping
    public ResponseEntity<List<MealSchedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getScheduleById(@PathVariable Long id) {
        return scheduleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Optional<MealSchedule> scheduleOpt = scheduleRepository.findById(id);
        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MealSchedule schedule = scheduleOpt.get();
        if (request.containsKey("mealTime")) schedule.setMealTime(request.get("mealTime").trim());
        if (request.containsKey("preparationTime")) schedule.setPreparationTime(request.get("preparationTime").trim());
        if (request.containsKey("attendanceDeadline")) schedule.setAttendanceDeadline(request.get("attendanceDeadline").trim());
        if (request.containsKey("cutoffTime")) schedule.setAttendanceDeadline(request.get("cutoffTime").trim());

        MealSchedule saved = scheduleRepository.save(schedule);
        return ResponseEntity.ok(Map.of("success", true, "message", "Meal schedule updated successfully.", "schedule", saved));
    }
}

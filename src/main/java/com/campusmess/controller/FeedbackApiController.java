package com.campusmess.controller;

import com.campusmess.entity.Feedback;
import com.campusmess.entity.User;
import com.campusmess.repository.FeedbackRepository;
import com.campusmess.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackApiController {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackApiController(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("studentId") || !request.containsKey("rating")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Student ID and rating are required."));
        }

        Long studentId = Long.valueOf(request.get("studentId").toString());
        int rating = Integer.parseInt(request.get("rating").toString());
        String mealType = request.getOrDefault("mealType", "LUNCH").toString().toUpperCase();
        String date = request.getOrDefault("date", java.time.LocalDate.now().toString()).toString();
        String comments = request.getOrDefault("comments", "").toString().trim();

        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Rating must be between 1 and 5."));
        }

        Optional<User> studentOpt = userRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Student not found."));
        }

        Feedback feedback = new Feedback(studentOpt.get(), mealType, date, rating, comments);
        Feedback saved = feedbackRepository.save(feedback);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Feedback submitted successfully.");
        response.put("feedback", toFeedbackMap(saved));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyFeedback(@RequestParam Long studentId) {
        List<Feedback> list = feedbackRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        List<Map<String, Object>> mapped = list.stream().map(this::toFeedbackMap).toList();
        return ResponseEntity.ok(mapped);
    }

    @GetMapping
    public ResponseEntity<?> getAllFeedback(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String date) {

        List<Feedback> list = feedbackRepository.findAllByOrderByCreatedAtDesc();

        if (rating != null) {
            list = list.stream().filter(f -> f.getRating() == rating).toList();
        }
        if (mealType != null && !mealType.trim().isEmpty()) {
            list = list.stream().filter(f -> mealType.equalsIgnoreCase(f.getMealType())).toList();
        }
        if (date != null && !date.trim().isEmpty()) {
            list = list.stream().filter(f -> date.equalsIgnoreCase(f.getDate())).toList();
        }

        double avgRating = list.isEmpty() ? 0.0 : list.stream().mapToInt(Feedback::getRating).average().orElse(0.0);
        Map<Integer, Long> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int r = i;
            breakdown.put(r, list.stream().filter(f -> f.getRating() == r).count());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", list.size());
        stats.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
        stats.put("breakdown", breakdown);

        Map<String, Object> res = new HashMap<>();
        res.put("feedback", list.stream().map(this::toFeedbackMap).toList());
        res.put("stats", stats);

        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        if (!feedbackRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        feedbackRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Feedback deleted successfully."));
    }

    private Map<String, Object> toFeedbackMap(Feedback f) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", f.getId());
        m.put("mealType", f.getMealType());
        m.put("date", f.getDate());
        m.put("rating", f.getRating());
        m.put("comments", f.getComments());
        m.put("createdAt", f.getCreatedAt());
        if (f.getStudent() != null) {
            m.put("studentId", f.getStudent().getId());
            m.put("studentName", f.getStudent().getName());
            m.put("email", f.getStudent().getEmail());
            if (f.getStudent().getProfile() != null) {
                m.put("rollNo", f.getStudent().getProfile().getStudentId());
                m.put("hostelBlock", f.getStudent().getProfile().getHostelBlock());
            }
        }
        return m;
    }
}

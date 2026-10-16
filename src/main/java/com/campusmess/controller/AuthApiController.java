package com.campusmess.controller;

import com.campusmess.entity.StudentProfile;
import com.campusmess.entity.User;
import com.campusmess.repository.StudentProfileRepository;
import com.campusmess.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthApiController {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthApiController(UserRepository userRepository, StudentProfileRepository profileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String phone = request.get("phone");
        String password = request.get("password");
        String confirmPassword = request.get("confirmPassword");
        String role = request.getOrDefault("role", "STUDENT").toUpperCase();
        String studentId = request.get("studentId");

        // Validation
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Full name is required."));
        }
        if (email == null || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Please enter a valid email address."));
        }
        if (phone == null || phone.trim().length() < 7) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Valid phone number is required."));
        }
        if (password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Password must be at least 8 characters long."));
        }
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Password confirmation does not match."));
        }
        if ("ADMIN".equals(role)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Administrator accounts cannot be registered publicly."));
        }
        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", "An account with this email address already exists."));
        }

        // Save User
        User user = new User(name.trim(), email.trim().toLowerCase(), phone.trim(),
                passwordEncoder.encode(password), role, "ACTIVE");
        user = userRepository.save(user);

        // If student, save StudentProfile
        if ("STUDENT".equals(role)) {
            String stuId = (studentId != null && !studentId.trim().isEmpty())
                    ? studentId.trim()
                    : "STU-" + LocalDate.now().getYear() + "-" + String.format("%03d", user.getId());
            StudentProfile profile = new StudentProfile(user, stuId, "Block A", "101", "");
            profileRepository.save(profile);
            user.setProfile(profile);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Registration successful. Please login.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email and password are required."));
        }

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email.trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Invalid email or password."));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Invalid email or password."));
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Your account has been deactivated. Please contact administrator."));
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("role", user.getRole().toLowerCase());
        userData.put("status", user.getStatus());

        if (user.getProfile() != null) {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("student_id", user.getProfile().getStudentId());
            profileData.put("hostel_block", user.getProfile().getHostelBlock());
            profileData.put("room_number", user.getProfile().getRoomNumber());
            userData.put("profile", profileData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Login successful.");
        response.put("user", userData);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates) {
        Long userId = Long.valueOf(updates.get("userId").toString());
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (updates.containsKey("name")) user.setName(updates.get("name").toString().trim());
        if (updates.containsKey("phone")) user.setPhone(updates.get("phone").toString().trim());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            Optional<StudentProfile> profileOpt = profileRepository.findByUserId(user.getId());
            StudentProfile profile = profileOpt.orElseGet(() -> new StudentProfile(user, "STU-" + user.getId(), "Block A", "101", ""));
            if (updates.containsKey("studentId")) profile.setStudentId(updates.get("studentId").toString());
            if (updates.containsKey("hostelBlock")) profile.setHostelBlock(updates.get("hostelBlock").toString());
            if (updates.containsKey("roomNumber")) profile.setRoomNumber(updates.get("roomNumber").toString());
            profileRepository.save(profile);
            user.setProfile(profile);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully."));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.get("userId"));
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "New password must be at least 8 characters long."));
        }
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Password confirmation does not match."));
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Current password is incorrect."));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully."));
    }
}

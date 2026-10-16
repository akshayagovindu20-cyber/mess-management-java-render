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
import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserApiController {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserApiController(UserRepository userRepository,
                             StudentProfileRepository profileRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {

        List<User> list;
        if (q != null && !q.trim().isEmpty()) {
            list = userRepository.searchUsers(q.trim());
        } else if (role != null && status != null) {
            list = userRepository.findByRoleIgnoreCaseAndStatusIgnoreCase(role.trim(), status.trim());
        } else if (role != null) {
            list = userRepository.findByRoleIgnoreCase(role.trim());
        } else if (status != null) {
            list = userRepository.findByStatusIgnoreCase(status.trim());
        } else {
            list = userRepository.findAll();
        }

        List<Map<String, Object>> mapped = list.stream().map(this::toUserMap).toList();
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toUserMap(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String phone = request.get("phone");
        String password = request.getOrDefault("password", "Pass@123");
        String role = request.getOrDefault("role", "STUDENT").toUpperCase();
        String status = request.getOrDefault("status", "ACTIVE").toUpperCase();

        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Name and email are required."));
        }

        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", "A user with this email already exists."));
        }

        User user = new User(name.trim(), email.trim().toLowerCase(), phone != null ? phone.trim() : "",
                passwordEncoder.encode(password), role, status);
        User saved = userRepository.save(user);

        if ("STUDENT".equals(role)) {
            String stuId = request.getOrDefault("studentId", "STU-" + LocalDate.now().getYear() + "-" + String.format("%03d", saved.getId()));
            String block = request.getOrDefault("hostelBlock", "Block A");
            String room = request.getOrDefault("roomNumber", "101");
            StudentProfile profile = new StudentProfile(saved, stuId, block, room, "");
            profileRepository.save(profile);
            saved.setProfile(profile);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "User created successfully.",
                "user", toUserMap(saved)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        if (request.containsKey("name")) user.setName(request.get("name").toString().trim());
        if (request.containsKey("phone")) user.setPhone(request.get("phone").toString().trim());
        if (request.containsKey("role")) user.setRole(request.get("role").toString().trim().toUpperCase());
        if (request.containsKey("status")) user.setStatus(request.get("status").toString().trim().toUpperCase());
        if (request.containsKey("password") && !request.get("password").toString().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.get("password").toString()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            Optional<StudentProfile> profileOpt = profileRepository.findByUserId(user.getId());
            StudentProfile profile = profileOpt.orElseGet(() -> new StudentProfile(user, "STU-" + user.getId(), "Block A", "101", ""));
            if (request.containsKey("studentId")) profile.setStudentId(request.get("studentId").toString());
            if (request.containsKey("hostelBlock")) profile.setHostelBlock(request.get("hostelBlock").toString());
            if (request.containsKey("roomNumber")) profile.setRoomNumber(request.get("roomNumber").toString());
            profileRepository.save(profile);
            user.setProfile(profile);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "User updated successfully.", "user", toUserMap(user)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        String newStatus;
        if (body != null && body.containsKey("status")) {
            newStatus = body.get("status").toUpperCase();
        } else {
            newStatus = "ACTIVE".equalsIgnoreCase(user.getStatus()) ? "INACTIVE" : "ACTIVE";
        }
        user.setStatus(newStatus);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "User status changed to " + newStatus, "status", newStatus));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        if ("ADMIN".equalsIgnoreCase(user.getRole()) && userRepository.findByRoleIgnoreCase("ADMIN").size() <= 1) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Cannot delete the sole Administrator account."));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully."));
    }

    private Map<String, Object> toUserMap(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("role", u.getRole().toLowerCase());
        m.put("status", u.getStatus());
        m.put("createdAt", u.getCreatedAt());
        if (u.getProfile() != null) {
            m.put("studentId", u.getProfile().getStudentId());
            m.put("hostelBlock", u.getProfile().getHostelBlock());
            m.put("roomNumber", u.getProfile().getRoomNumber());
        }
        return m;
    }
}

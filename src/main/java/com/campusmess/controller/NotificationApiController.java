package com.campusmess.controller;

import com.campusmess.entity.Notification;
import com.campusmess.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationApiController {

    private final NotificationRepository notificationRepository;

    public NotificationApiController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestParam(required = false) Long userId) {
        List<Notification> list;
        if (userId != null) {
            list = notificationRepository.findByUserIdOrUserIdIsNullOrderByCreatedAtDesc(userId);
        } else {
            list = notificationRepository.findAllByOrderByCreatedAtDesc();
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody Map<String, Object> request) {
        String title = request.get("title") != null ? request.get("title").toString().trim() : null;
        String message = request.get("message") != null ? request.get("message").toString().trim() : null;
        String type = request.getOrDefault("type", "INFO").toString().trim().toUpperCase();
        Long userId = request.get("userId") != null ? Long.valueOf(request.get("userId").toString()) : null;

        if (title == null || title.isEmpty() || message == null || message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Title and message are required."));
        }

        Notification notification = new Notification(userId, title, message, type);
        Notification saved = notificationRepository.save(notification);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Notification broadcasted successfully.",
                "notification", saved
        ));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Notification notif = opt.get();
        notif.setRead(true);
        notificationRepository.save(notif);

        return ResponseEntity.ok(Map.of("success", true, "message", "Notification marked as read."));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestParam(required = false) Long userId) {
        List<Notification> list = (userId != null)
                ? notificationRepository.findByUserIdOrUserIdIsNullOrderByCreatedAtDesc(userId)
                : notificationRepository.findAll();

        for (Notification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);

        return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read."));
    }
}

package com.campusmess.controller;

import com.campusmess.entity.*;
import com.campusmess.repository.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReportApiController {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final MealScheduleRepository scheduleRepository;
    private final MealAttendanceRepository attendanceRepository;
    private final FeedbackRepository feedbackRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;
    private final NotificationRepository notificationRepository;

    public ReportApiController(UserRepository userRepository,
                               MenuRepository menuRepository,
                               MealScheduleRepository scheduleRepository,
                               MealAttendanceRepository attendanceRepository,
                               FeedbackRepository feedbackRepository,
                               PaymentRepository paymentRepository,
                               InventoryRepository inventoryRepository,
                               NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.scheduleRepository = scheduleRepository;
        this.attendanceRepository = attendanceRepository;
        this.feedbackRepository = feedbackRepository;
        this.paymentRepository = paymentRepository;
        this.inventoryRepository = inventoryRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Campus Mess Management Spring Boot Application");
        health.put("timestamp", LocalDateTime.now());
        health.put("javaVersion", System.getProperty("java.version"));
        return ResponseEntity.ok(health);
    }

    @GetMapping("/reports/dashboard")
    public ResponseEntity<?> getDashboardStats(
            @RequestParam(defaultValue = "student") String role,
            @RequestParam(required = false) Long userId) {

        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        Map<String, Object> data = new HashMap<>();
        data.put("role", role.toLowerCase());
        data.put("date", today);

        if ("student".equalsIgnoreCase(role)) {
            List<Menu> todayMenus = menuRepository.findByDateOrderByMealType(today);
            List<Menu> tomorrowMenus = menuRepository.findByDateOrderByMealType(tomorrow);
            List<MealAttendance> myAttendances = (userId != null)
                    ? attendanceRepository.findByStudentIdAndDate(userId, today)
                    : Collections.emptyList();

            long totalMeals = (userId != null) ? attendanceRepository.findByStudentIdOrderByDateDesc(userId).size() : 0;
            long attended = (userId != null) ? attendanceRepository.findByStudentIdOrderByDateDesc(userId).stream()
                    .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count() : 0;

            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            boolean feePaid = (userId != null) && paymentRepository.findByStudentIdOrderByPaymentDateDesc(userId).stream()
                    .anyMatch(p -> currentMonth.equalsIgnoreCase(p.getMonthYear()) && "PAID".equalsIgnoreCase(p.getPaymentStatus()));

            data.put("todayMenus", todayMenus);
            data.put("tomorrowMenus", tomorrowMenus);
            data.put("todayAttendances", myAttendances);
            data.put("attendancePercentage", totalMeals > 0 ? Math.round(((double) attended / totalMeals) * 100.0) : 100);
            data.put("feeStatus", feePaid ? "PAID" : "PENDING");
            data.put("monthlyFee", 3200);

        } else if ("manager".equalsIgnoreCase(role)) {
            long totalStudents = userRepository.findByRoleIgnoreCase("STUDENT").size();
            List<MealAttendance> todayAttendances = attendanceRepository.findByDate(today);
            long presentToday = todayAttendances.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
            List<InventoryItem> lowStock = inventoryRepository.findLowStockItems();
            List<Feedback> allFeedback = feedbackRepository.findAll();
            double avgRating = allFeedback.isEmpty() ? 0.0 : allFeedback.stream().mapToInt(Feedback::getRating).average().orElse(0.0);

            BigDecimal totalRevenue = paymentRepository.findAll().stream()
                    .filter(p -> "PAID".equalsIgnoreCase(p.getPaymentStatus()))
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            data.put("totalStudents", totalStudents);
            data.put("presentToday", presentToday);
            data.put("lowStockCount", lowStock.size());
            data.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
            data.put("totalRevenue", totalRevenue);
            data.put("menusToday", menuRepository.findByDateOrderByMealType(today).size());

        } else if ("cook".equalsIgnoreCase(role)) {
            long totalStudents = userRepository.findByRoleIgnoreCase("STUDENT").size();
            List<MealAttendance> todayAttendances = attendanceRepository.findByDate(today);

            Map<String, Object> servings = new HashMap<>();
            String[] meals = {"BREAKFAST", "LUNCH", "DINNER"};
            for (String m : meals) {
                long pres = todayAttendances.stream()
                        .filter(a -> m.equalsIgnoreCase(a.getMealType()) && "PRESENT".equalsIgnoreCase(a.getStatus()))
                        .count();
                servings.put(m.toLowerCase(), pres > 0 ? pres : Math.max(1, (long)(totalStudents * 0.85)));
            }

            data.put("servings", servings);
            data.put("totalStudents", totalStudents);
            data.put("schedules", scheduleRepository.findAll());
            data.put("todayMenus", menuRepository.findByDateOrderByMealType(today));
            data.put("lowStockCount", inventoryRepository.findLowStockItems().size());

        } else if ("admin".equalsIgnoreCase(role)) {
            List<User> users = userRepository.findAll();
            long students = users.stream().filter(u -> "STUDENT".equalsIgnoreCase(u.getRole())).count();
            long managers = users.stream().filter(u -> "MANAGER".equalsIgnoreCase(u.getRole())).count();
            long cooks = users.stream().filter(u -> "COOK".equalsIgnoreCase(u.getRole())).count();
            long admins = users.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).count();
            long active = users.stream().filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus())).count();

            BigDecimal totalRevenue = paymentRepository.findAll().stream()
                    .filter(p -> "PAID".equalsIgnoreCase(p.getPaymentStatus()))
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            data.put("totalUsers", users.size());
            data.put("studentsCount", students);
            data.put("managersCount", managers);
            data.put("cooksCount", cooks);
            data.put("adminsCount", admins);
            data.put("activeUsers", active);
            data.put("totalRevenue", totalRevenue);
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/reports/export")
    public ResponseEntity<String> exportCsv(@RequestParam(defaultValue = "attendance") String type) {
        StringBuilder csv = new StringBuilder();
        String filename = type.toLowerCase() + "_report.csv";

        switch (type.toLowerCase()) {
            case "attendance" -> {
                csv.append("ID,Date,Meal Type,Student ID,Student Name,Status,Marked At\n");
                List<MealAttendance> list = attendanceRepository.findAll();
                for (MealAttendance a : list) {
                    csv.append(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                            a.getId(), a.getDate(), a.getMealType(),
                            a.getStudent() != null ? a.getStudent().getId() : "",
                            a.getStudent() != null ? escapeCsv(a.getStudent().getName()) : "",
                            a.getStatus(),
                            a.getMarkedAt()));
                }
            }
            case "payments" -> {
                csv.append("ID,Transaction ID,Student ID,Student Name,Month,Amount,Payment Method,Status,Payment Date\n");
                List<Payment> list = paymentRepository.findAll();
                for (Payment p : list) {
                    csv.append(String.format("%d,%s,%s,%s,%s,%.2f,%s,%s,%s\n",
                            p.getId(), p.getTransactionId(),
                            p.getStudent() != null ? p.getStudent().getId() : "",
                            p.getStudent() != null ? escapeCsv(p.getStudent().getName()) : "",
                            p.getMonthYear(), p.getAmount(),
                            escapeCsv(p.getPaymentMethod()), p.getPaymentStatus(),
                            p.getPaymentDate()));
                }
            }
            case "inventory" -> {
                csv.append("ID,Item Name,Category,Quantity,Unit,Minimum Stock,Status,Last Restocked\n");
                List<InventoryItem> list = inventoryRepository.findAll();
                for (InventoryItem i : list) {
                    boolean low = i.getQuantity().compareTo(i.getMinimumStock()) <= 0;
                    csv.append(String.format("%d,%s,%s,%.2f,%s,%.2f,%s,%s\n",
                            i.getId(), escapeCsv(i.getItemName()), escapeCsv(i.getCategory()),
                            i.getQuantity(), i.getUnit(), i.getMinimumStock(),
                            low ? "LOW STOCK" : "OPTIMAL",
                            i.getLastRestocked()));
                }
            }
            case "users" -> {
                csv.append("ID,Full Name,Email,Phone,Role,Status,Student ID,Hostel Block,Room No\n");
                List<User> list = userRepository.findAll();
                for (User u : list) {
                    String sId = u.getProfile() != null ? u.getProfile().getStudentId() : "";
                    String blk = u.getProfile() != null ? u.getProfile().getHostelBlock() : "";
                    String rm = u.getProfile() != null ? u.getProfile().getRoomNumber() : "";
                    csv.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            u.getId(), escapeCsv(u.getName()), u.getEmail(),
                            u.getPhone(), u.getRole(), u.getStatus(),
                            sId, blk, rm));
                }
            }
            case "feedback" -> {
                csv.append("ID,Date,Meal Type,Student Name,Rating,Comments,Submitted At\n");
                List<Feedback> list = feedbackRepository.findAll();
                for (Feedback f : list) {
                    csv.append(String.format("%d,%s,%s,%s,%d,%s,%s\n",
                            f.getId(), f.getDate(), f.getMealType(),
                            f.getStudent() != null ? escapeCsv(f.getStudent().getName()) : "",
                            f.getRating(),
                            escapeCsv(f.getComments()),
                            f.getCreatedAt()));
                }
            }
            default -> csv.append("Invalid report type specified.\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}

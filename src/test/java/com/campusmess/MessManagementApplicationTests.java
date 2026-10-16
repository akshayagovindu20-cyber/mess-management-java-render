package com.campusmess;

import com.campusmess.entity.MealSchedule;
import com.campusmess.entity.User;
import com.campusmess.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mess-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.sql.init.mode=never"
})
class MessManagementApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MealScheduleRepository scheduleRepository;

    @Autowired
    private MealAttendanceRepository attendanceRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        assertNotNull(userRepository);
        assertNotNull(menuRepository);
    }

    @Test
    void testPreseededDemoAccounts() {
        Optional<User> student = userRepository.findByEmailIgnoreCase("student@mess.com");
        assertTrue(student.isPresent(), "Student account should be pre-seeded");
        assertTrue(passwordEncoder.matches("Student@123", student.get().getPassword()));
        assertEquals("STUDENT", student.get().getRole());

        Optional<User> manager = userRepository.findByEmailIgnoreCase("manager@mess.com");
        assertTrue(manager.isPresent(), "Manager account should be pre-seeded");
        assertTrue(passwordEncoder.matches("Manager@123", manager.get().getPassword()));
        assertEquals("MANAGER", manager.get().getRole());

        Optional<User> cook = userRepository.findByEmailIgnoreCase("cook@mess.com");
        assertTrue(cook.isPresent(), "Cook account should be pre-seeded");
        assertTrue(passwordEncoder.matches("Cook@123", cook.get().getPassword()));
        assertEquals("COOK", cook.get().getRole());

        Optional<User> admin = userRepository.findByEmailIgnoreCase("admin@mess.com");
        assertTrue(admin.isPresent(), "Admin account should be pre-seeded");
        assertTrue(passwordEncoder.matches("Admin@123", admin.get().getPassword()));
        assertEquals("ADMIN", admin.get().getRole());
    }

    @Test
    void testMealSchedulesSeeded() {
        List<MealSchedule> schedules = scheduleRepository.findAll();
        assertEquals(3, schedules.size(), "Should have Breakfast, Lunch, and Dinner schedules");
    }

    @Test
    void testMenusAvailable() {
        String today = LocalDate.now().toString();
        var todayMenus = menuRepository.findByDateOrderByMealType(today);
        assertFalse(todayMenus.isEmpty(), "Today's menus should be pre-seeded");
    }

    @Test
    void testInventoryItemsAvailable() {
        var items = inventoryRepository.findAll();
        assertTrue(items.size() >= 5, "Inventory should contain seeded food items");
    }
}

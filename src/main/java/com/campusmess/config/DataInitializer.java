package com.campusmess.config;

import com.campusmess.entity.*;
import com.campusmess.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final MealScheduleRepository scheduleRepository;
    private final MenuRepository menuRepository;
    private final MealAttendanceRepository attendanceRepository;
    private final FeedbackRepository feedbackRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            StudentProfileRepository profileRepository,
            MealScheduleRepository scheduleRepository,
            MenuRepository menuRepository,
            MealAttendanceRepository attendanceRepository,
            FeedbackRepository feedbackRepository,
            PaymentRepository paymentRepository,
            InventoryRepository inventoryRepository,
            NotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.scheduleRepository = scheduleRepository;
        this.menuRepository = menuRepository;
        this.attendanceRepository = attendanceRepository;
        this.feedbackRepository = feedbackRepository;
        this.paymentRepository = paymentRepository;
        this.inventoryRepository = inventoryRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Already initialized
        }

        System.out.println("Initializing realistic CampusMess demonstration dataset...");

        // 1. Users & Student Profiles
        User student = new User("Alex Johnson", "student@mess.com", "+1 555-019-2834",
                passwordEncoder.encode("Student@123"), "STUDENT", "ACTIVE");
        userRepository.save(student);
        StudentProfile p1 = new StudentProfile(student, "STU-2026-081", "Block A (Mandela)", "204-B", "");
        profileRepository.save(p1);

        User manager = new User("Rajesh Kumar", "manager@mess.com", "+1 555-014-9921",
                passwordEncoder.encode("Manager@123"), "MANAGER", "ACTIVE");
        userRepository.save(manager);

        User cook = new User("Chef Ramesh Singh", "cook@mess.com", "+1 555-018-7711",
                passwordEncoder.encode("Cook@123"), "COOK", "ACTIVE");
        userRepository.save(cook);

        User admin = new User("Dr. Eleanor Vance", "admin@mess.com", "+1 555-012-3456",
                passwordEncoder.encode("Admin@123"), "ADMIN", "ACTIVE");
        userRepository.save(admin);

        // Additional Students
        User s2 = new User("Priya Sharma", "priya.sharma@mess.com", "+1 555-019-1122",
                passwordEncoder.encode("Student@123"), "STUDENT", "ACTIVE");
        userRepository.save(s2);
        profileRepository.save(new StudentProfile(s2, "STU-2026-092", "Block B (Curie)", "112-A", ""));

        User s3 = new User("Rohan Patel", "rohan.patel@mess.com", "+1 555-019-3344",
                passwordEncoder.encode("Student@123"), "STUDENT", "ACTIVE");
        userRepository.save(s3);
        profileRepository.save(new StudentProfile(s3, "STU-2026-103", "Block A (Mandela)", "305-C", ""));

        User s4 = new User("Sneha Iyer", "sneha.iyer@mess.com", "+1 555-019-5566",
                passwordEncoder.encode("Student@123"), "STUDENT", "ACTIVE");
        userRepository.save(s4);
        profileRepository.save(new StudentProfile(s4, "STU-2026-114", "Block C (Kalam)", "401-A", ""));

        // 2. Meal Schedules
        scheduleRepository.save(new MealSchedule("breakfast", "07:30 AM - 09:30 AM", "05:30 AM - 07:15 AM", "07:00 AM"));
        scheduleRepository.save(new MealSchedule("lunch", "12:30 PM - 02:30 PM", "10:30 AM - 12:15 PM", "11:30 AM"));
        scheduleRepository.save(new MealSchedule("dinner", "07:30 PM - 09:30 PM", "05:30 PM - 07:15 PM", "06:30 PM"));

        // 3. Menus
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String dayAfter = LocalDate.now().plusDays(2).toString();

        Menu m1 = new Menu(today, "breakfast", "Masala Dosa, Sambhar, Coconut Chutney, Boiled Eggs, Fresh Fruits, Filter Coffee/Tea",
                "Crispy fermented rice & lentil crepes served piping hot with lentil soup.", "07:30 AM - 09:30 AM", manager.getId());
        Menu m2 = new Menu(today, "lunch", "Jeera Rice, Shahi Paneer, Dal Makhani, Fresh Chapati, Cucumber Raita, Gulab Jamun",
                "North Indian feast with cottage cheese in cashew tomato gravy and slow-cooked lentils.", "12:30 PM - 02:30 PM", manager.getId());
        Menu m3 = new Menu(today, "dinner", "Steamed Basmati Rice, Chicken Curry / Kadai Mushroom, Mix Veg Pulao, Tawa Roti, Salad",
                "High protein chicken or mushroom options cooked in homestyle spices.", "07:30 PM - 09:30 PM", manager.getId());

        Menu m4 = new Menu(tomorrow, "breakfast", "Aloo Paratha, Curd, Mixed Pickle, Mint Chutney, Butter Cubes, Tea/Coffee",
                "Stuffed whole wheat potato flatbreads with churned yogurt.", "07:30 AM - 09:30 AM", manager.getId());
        Menu m5 = new Menu(tomorrow, "lunch", "Hyderabadi Veg Biryani, Mirchi Ka Salan, Boondi Raita, Onion Salad, Roasted Papad",
                "Fragrant layered basmati rice with whole saffron and spices.", "12:30 PM - 02:30 PM", manager.getId());
        Menu m6 = new Menu(tomorrow, "dinner", "Yellow Dal Tadka, Gobi Aloo Masala, Phulka Rotis, Steamed Rice, Fruit Custard",
                "Wholesome dinner with tempered yellow lentils and fresh fruit dessert.", "07:30 PM - 09:30 PM", manager.getId());

        Menu m7 = new Menu(dayAfter, "breakfast", "Idli, Vada, Sambar, Coconut Chutney, Banana, Hot Milk & Tea",
                "Steamed idlis and crispy medu vadas.", "07:30 AM - 09:30 AM", manager.getId());
        Menu m8 = new Menu(dayAfter, "lunch", "Rajma Masala, Kashmiri Pulao, Curd, Tandoori Roti, Kachumber Salad",
                "Red kidney beans in onion-tomato gravy with spiced rice.", "12:30 PM - 02:30 PM", manager.getId());
        Menu m9 = new Menu(dayAfter, "dinner", "Paneer Butter Masala, Egg Curry, Peas Pulao, Butter Naan, Ice Cream Cup",
                "Special weekend dinner banquet.", "07:30 PM - 09:30 PM", manager.getId());

        menuRepository.save(m1); menuRepository.save(m2); menuRepository.save(m3);
        menuRepository.save(m4); menuRepository.save(m5); menuRepository.save(m6);
        menuRepository.save(m7); menuRepository.save(m8); menuRepository.save(m9);

        // 4. Meal Attendance
        attendanceRepository.save(new MealAttendance(student, m1.getId(), "breakfast", today, "PRESENT"));
        attendanceRepository.save(new MealAttendance(student, m2.getId(), "lunch", today, "PRESENT"));
        attendanceRepository.save(new MealAttendance(s2, m1.getId(), "breakfast", today, "PRESENT"));
        attendanceRepository.save(new MealAttendance(s2, m2.getId(), "lunch", today, "PRESENT"));
        attendanceRepository.save(new MealAttendance(s3, m1.getId(), "breakfast", today, "PRESENT"));
        attendanceRepository.save(new MealAttendance(s4, m1.getId(), "breakfast", today, "SKIPPED"));

        // Past attendances for metrics
        String yesterday = LocalDate.now().minusDays(1).toString();
        String twoDaysAgo = LocalDate.now().minusDays(2).toString();
        attendanceRepository.save(new MealAttendance(student, null, "breakfast", yesterday, "PRESENT"));
        attendanceRepository.save(new MealAttendance(student, null, "lunch", yesterday, "PRESENT"));
        attendanceRepository.save(new MealAttendance(student, null, "dinner", yesterday, "PRESENT"));
        attendanceRepository.save(new MealAttendance(student, null, "breakfast", twoDaysAgo, "PRESENT"));
        attendanceRepository.save(new MealAttendance(student, null, "lunch", twoDaysAgo, "SKIPPED"));
        attendanceRepository.save(new MealAttendance(student, null, "dinner", twoDaysAgo, "PRESENT"));

        // 5. Feedback
        feedbackRepository.save(new Feedback(student, "breakfast", today, 5, "Delicious masala dosas today! Coconut chutney was fresh and sambhar was perfectly spiced."));
        feedbackRepository.save(new Feedback(s2, "breakfast", today, 4, "Good quality breakfast. Would appreciate warmer filter coffee during second batch."));
        feedbackRepository.save(new Feedback(s3, "dinner", yesterday, 5, "Kadai mushroom was superb. Rotis were very soft."));
        feedbackRepository.save(new Feedback(s4, "lunch", yesterday, 4, "Dal makhani was creamy and rich. Overall great service."));
        feedbackRepository.save(new Feedback(student, "lunch", twoDaysAgo, 3, "Biryani was tasty but could use slightly less spice for standard dietary tolerance."));

        // 6. Payments
        paymentRepository.save(new Payment(student, new BigDecimal("4500.00"), "TXN-2026-SEP-882194", "PAID", "September 2026"));
        paymentRepository.save(new Payment(student, new BigDecimal("4500.00"), "TXN-2026-OCT-990145", "PENDING", "October 2026"));
        paymentRepository.save(new Payment(s2, new BigDecimal("4500.00"), "TXN-2026-OCT-112044", "PAID", "October 2026"));
        paymentRepository.save(new Payment(s3, new BigDecimal("4500.00"), "TXN-2026-OCT-184920", "PAID", "October 2026"));
        paymentRepository.save(new Payment(s4, new BigDecimal("4500.00"), "TXN-2026-OCT-201889", "PENDING", "October 2026"));

        // 7. Inventory
        inventoryRepository.save(new InventoryItem("Basmati Rice", "Grains & Cereals", new BigDecimal("45.00"), "kg", new BigDecimal("50.00"), "AgroFresh Wholesale Ltd.")); // Low stock!
        inventoryRepository.save(new InventoryItem("Chakki Fresh Wheat Flour", "Grains & Cereals", new BigDecimal("180.00"), "kg", new BigDecimal("60.00"), "Punjab Agro Supplies"));
        inventoryRepository.save(new InventoryItem("Toor Dal (Pigeon Pea)", "Pulses & Lentils", new BigDecimal("18.50"), "kg", new BigDecimal("25.00"), "Mahalaxmi Pulses")); // Low stock!
        inventoryRepository.save(new InventoryItem("Refined Sunflower Oil", "Oils & Ghee", new BigDecimal("65.00"), "liters", new BigDecimal("30.00"), "Fortune Edible Oils"));
        inventoryRepository.save(new InventoryItem("Fresh Whole Milk", "Dairy", new BigDecimal("90.00"), "liters", new BigDecimal("40.00"), "Amul Dairy Cooperative"));
        inventoryRepository.save(new InventoryItem("Fresh Paneer", "Dairy", new BigDecimal("12.00"), "kg", new BigDecimal("15.00"), "Amul Dairy Cooperative")); // Low stock!
        inventoryRepository.save(new InventoryItem("Farm Fresh Eggs", "Poultry", new BigDecimal("360.00"), "pcs", new BigDecimal("120.00"), "Sunrise Poultry Farms"));
        inventoryRepository.save(new InventoryItem("Potatoes", "Vegetables", new BigDecimal("110.00"), "kg", new BigDecimal("40.00"), "City Mandi Traders"));
        inventoryRepository.save(new InventoryItem("Red Onions", "Vegetables", new BigDecimal("75.00"), "kg", new BigDecimal("35.00"), "City Mandi Traders"));
        inventoryRepository.save(new InventoryItem("Assam CTC Tea Leaves", "Beverages", new BigDecimal("22.00"), "kg", new BigDecimal("10.00"), "Assam Valley Tea Co."));

        // 8. Notifications
        notificationRepository.save(new Notification(null, "Weekly Special Menu Published",
                "The mess committee has uploaded the special weekend feast schedule. Please review upcoming menus.", "MENU", false));
        notificationRepository.save(new Notification(student.getId(), "October Mess Fee Due Reminder",
                "Kindly settle your mess dues for October 2026 before October 15 to avoid late fee penalties.", "PAYMENT", false));
        notificationRepository.save(new Notification(manager.getId(), "Low Inventory Alert: Basmati Rice & Toor Dal",
                "Current stock for Basmati Rice (45kg) and Toor Dal (18.5kg) has dropped below threshold limits. Reorder required.", "INVENTORY", false));
        notificationRepository.save(new Notification(null, "Kitchen Deep Sanitization Schedule",
                "Regular kitchen equipment deep cleaning and steam sanitization will be conducted Sunday 3:00 PM - 5:00 PM.", "ANNOUNCEMENT", true));

        System.out.println("CampusMess dataset initialized successfully!");
    }
}

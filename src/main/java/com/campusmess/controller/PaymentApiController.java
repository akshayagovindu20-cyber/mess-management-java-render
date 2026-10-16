package com.campusmess.controller;

import com.campusmess.entity.Payment;
import com.campusmess.entity.User;
import com.campusmess.repository.PaymentRepository;
import com.campusmess.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentApiController {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentApiController(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyPayments(@RequestParam Long studentId) {
        List<Payment> payments = paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId);

        String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        Optional<Payment> currentPaid = payments.stream()
                .filter(p -> currentMonthYear.equalsIgnoreCase(p.getMonthYear()) && "PAID".equalsIgnoreCase(p.getPaymentStatus()))
                .findFirst();

        Map<String, Object> feeStatus = new HashMap<>();
        feeStatus.put("currentMonth", currentMonthYear);
        feeStatus.put("monthlyFee", new BigDecimal("3200.00"));
        feeStatus.put("dueDate", LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).toString());
        feeStatus.put("isPaid", currentPaid.isPresent());
        feeStatus.put("status", currentPaid.isPresent() ? "PAID" : "PENDING");
        feeStatus.put("amountDue", currentPaid.isPresent() ? BigDecimal.ZERO : new BigDecimal("3200.00"));

        Map<String, Object> response = new HashMap<>();
        response.put("feeStatus", feeStatus);
        response.put("payments", payments.stream().map(this::toPaymentMap).toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("studentId") || !request.containsKey("amount")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Student ID and amount are required."));
        }

        Long studentId = Long.valueOf(request.get("studentId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String monthYear = request.getOrDefault("monthYear", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))).toString();
        String paymentMethod = request.getOrDefault("paymentMethod", "UPI / Net Banking").toString();

        Optional<User> studentOpt = userRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Student not found."));
        }

        // Generate realistic transaction ID: TXN-YYYYMMDD-XXXX
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String transactionId = "TXN-" + datePart + "-" + randPart;

        Payment payment = new Payment(studentOpt.get(), amount, monthYear, transactionId, paymentMethod, "PAID");
        Payment saved = paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Payment of ₹" + amount + " processed successfully. Transaction ID: " + transactionId);
        response.put("payment", toPaymentMap(saved));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllPayments(@RequestParam(required = false) String status) {
        List<Payment> list;
        if (status != null && !status.trim().isEmpty()) {
            list = paymentRepository.findByPaymentStatusIgnoreCase(status.trim());
        } else {
            list = paymentRepository.findAllByOrderByPaymentDateDesc();
        }

        BigDecimal totalRevenue = list.stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getPaymentStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> res = new HashMap<>();
        res.put("totalRevenue", totalRevenue);
        res.put("count", list.size());
        res.put("payments", list.stream().map(this::toPaymentMap).toList());

        return ResponseEntity.ok(res);
    }

    @GetMapping("/receipt/{id}")
    public ResponseEntity<?> getReceipt(@PathVariable Long id) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Payment p = paymentOpt.get();
        Map<String, Object> receipt = toPaymentMap(p);
        receipt.put("organization", "Campus Mess Facility & Hostel Services");
        receipt.put("taxInvoiceNumber", "INV-" + p.getTransactionId());
        receipt.put("mealPlan", "Standard 3-Meals Monthly Boarding Plan");

        return ResponseEntity.ok(receipt);
    }

    private Map<String, Object> toPaymentMap(Payment p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("amount", p.getAmount());
        m.put("monthYear", p.getMonthYear());
        m.put("transactionId", p.getTransactionId());
        m.put("paymentMethod", p.getPaymentMethod());
        m.put("paymentStatus", p.getPaymentStatus());
        m.put("paymentDate", p.getPaymentDate());
        if (p.getStudent() != null) {
            m.put("studentId", p.getStudent().getId());
            m.put("studentName", p.getStudent().getName());
            m.put("email", p.getStudent().getEmail());
            m.put("phone", p.getStudent().getPhone());
            if (p.getStudent().getProfile() != null) {
                m.put("rollNo", p.getStudent().getProfile().getStudentId());
                m.put("hostelBlock", p.getStudent().getProfile().getHostelBlock());
                m.put("roomNumber", p.getStudent().getProfile().getRoomNumber());
            }
        }
        return m;
    }
}

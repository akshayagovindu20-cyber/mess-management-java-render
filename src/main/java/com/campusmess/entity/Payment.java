package com.campusmess.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "UPI / Net Banking";

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus = "PAID"; // "PAID", "PENDING", "FAILED"

    @Column(name = "payment_date")
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(name = "month_year", nullable = false, length = 50)
    private String monthYear;

    public Payment() {}

    public Payment(User student, BigDecimal amount, String transactionId, String paymentStatus, String monthYear) {
        this.student = student;
        this.amount = amount;
        this.transactionId = transactionId;
        this.paymentStatus = paymentStatus;
        this.monthYear = monthYear;
        this.paymentDate = LocalDateTime.now();
    }

    public Payment(User student, BigDecimal amount, String monthYear, String transactionId, String paymentMethod, String paymentStatus) {
        this.student = student;
        this.amount = amount;
        this.monthYear = monthYear;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }
}

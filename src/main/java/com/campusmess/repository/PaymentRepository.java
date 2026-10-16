package com.campusmess.repository;

import com.campusmess.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId);
    Optional<Payment> findByStudentIdAndMonthYearIgnoreCase(Long studentId, String monthYear);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByPaymentStatusIgnoreCase(String paymentStatus);
    List<Payment> findAllByOrderByPaymentDateDesc();
}

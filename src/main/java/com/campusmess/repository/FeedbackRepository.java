package com.campusmess.repository;

import com.campusmess.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Feedback> findAllByOrderByCreatedAtDesc();
    List<Feedback> findByRating(int rating);
    List<Feedback> findByMealTypeIgnoreCase(String mealType);
    List<Feedback> findByDate(String date);
}

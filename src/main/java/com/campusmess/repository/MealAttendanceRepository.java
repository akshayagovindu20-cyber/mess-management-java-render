package com.campusmess.repository;

import com.campusmess.entity.MealAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealAttendanceRepository extends JpaRepository<MealAttendance, Long> {
    List<MealAttendance> findByStudentIdOrderByDateDesc(Long studentId);
    List<MealAttendance> findByStudentIdAndDate(Long studentId, String date);
    Optional<MealAttendance> findByStudentIdAndDateAndMealTypeIgnoreCase(Long studentId, String date, String mealType);
    List<MealAttendance> findByDate(String date);
    List<MealAttendance> findByDateAndMealTypeIgnoreCase(String date, String mealType);
    List<MealAttendance> findByDateBetweenOrderByDateDesc(String startDate, String endDate);
}

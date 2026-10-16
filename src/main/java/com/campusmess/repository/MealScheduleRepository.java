package com.campusmess.repository;

import com.campusmess.entity.MealSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealScheduleRepository extends JpaRepository<MealSchedule, Long> {
    Optional<MealSchedule> findByMealTypeIgnoreCase(String mealType);
}

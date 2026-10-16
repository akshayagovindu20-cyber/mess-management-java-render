package com.campusmess.repository;

import com.campusmess.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByDateOrderByMealType(String date);
    Optional<Menu> findByDateAndMealTypeIgnoreCase(String date, String mealType);
    List<Menu> findByDateBetweenOrderByDateAsc(String startDate, String endDate);
    List<Menu> findAllByOrderByDateAsc();
}

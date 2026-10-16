package com.campusmess.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "menus", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"date", "meal_type"})
})
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String date; // "YYYY-MM-DD"

    @Column(name = "meal_type", nullable = false, length = 30)
    private String mealType; // "breakfast", "lunch", "dinner"

    @Column(name = "food_items", nullable = false, columnDefinition = "TEXT")
    private String foodItems;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "meal_time", nullable = false, length = 50)
    private String mealTime;

    @Column(name = "created_by")
    private Long createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Menu() {}

    public Menu(String date, String mealType, String foodItems, String description, String mealTime, Long createdBy) {
        this.date = date;
        this.mealType = mealType;
        this.foodItems = foodItems;
        this.description = description;
        this.mealTime = mealTime;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public Menu(String date, String mealType, String foodItems, String description) {
        this(date, mealType, foodItems, description,
                "breakfast".equalsIgnoreCase(mealType) ? "07:30 AM - 09:30 AM" :
                "lunch".equalsIgnoreCase(mealType) ? "12:30 PM - 02:30 PM" : "07:30 PM - 09:30 PM", null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getFoodItems() { return foodItems; }
    public void setFoodItems(String foodItems) { this.foodItems = foodItems; }

    public String getItems() { return foodItems; }
    public void setItems(String items) { this.foodItems = items; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMealTime() { return mealTime; }
    public void setMealTime(String mealTime) { this.mealTime = mealTime; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.createdAt = updatedAt; }
}

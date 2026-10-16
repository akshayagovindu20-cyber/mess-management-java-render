package com.campusmess.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "meal_schedules")
public class MealSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_type", nullable = false, unique = true, length = 30)
    private String mealType; // "breakfast", "lunch", "dinner"

    @Column(name = "meal_time", nullable = false, length = 50)
    private String mealTime;

    @Column(name = "preparation_time", nullable = false, length = 50)
    private String preparationTime;

    @Column(name = "attendance_deadline", nullable = false, length = 50)
    private String attendanceDeadline;

    public MealSchedule() {}

    public MealSchedule(String mealType, String mealTime, String preparationTime, String attendanceDeadline) {
        this.mealType = mealType;
        this.mealTime = mealTime;
        this.preparationTime = preparationTime;
        this.attendanceDeadline = attendanceDeadline;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getMealTime() { return mealTime; }
    public void setMealTime(String mealTime) { this.mealTime = mealTime; }

    public String getPreparationTime() { return preparationTime; }
    public void setPreparationTime(String preparationTime) { this.preparationTime = preparationTime; }

    public String getAttendanceDeadline() { return attendanceDeadline; }
    public void setAttendanceDeadline(String attendanceDeadline) { this.attendanceDeadline = attendanceDeadline; }
}

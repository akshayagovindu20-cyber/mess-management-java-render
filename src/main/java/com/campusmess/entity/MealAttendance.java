package com.campusmess.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "meal_type", "date"})
})
public class MealAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "meal_type", nullable = false, length = 30)
    private String mealType; // "breakfast", "lunch", "dinner"

    @Column(nullable = false, length = 20)
    private String date; // "YYYY-MM-DD"

    @Column(nullable = false, length = 30)
    private String status = "PRESENT"; // "PRESENT", "SKIPPED"

    @Column(name = "marked_at")
    private LocalDateTime markedAt = LocalDateTime.now();

    public MealAttendance() {}

    public MealAttendance(User student, Long menuId, String mealType, String date, String status) {
        this.student = student;
        this.menuId = menuId;
        this.mealType = mealType;
        this.date = date;
        this.status = status;
        this.markedAt = LocalDateTime.now();
    }

    public MealAttendance(User student, String mealType, String date, String status) {
        this(student, null, mealType, date, status);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }
}

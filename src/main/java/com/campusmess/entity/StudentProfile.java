package com.campusmess.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "student_id", nullable = false, unique = true, length = 50)
    private String studentId;

    @Column(name = "hostel_block", length = 50)
    private String hostelBlock = "Block A";

    @Column(name = "room_number", length = 20)
    private String roomNumber = "101";

    @Column(name = "profile_picture", columnDefinition = "TEXT")
    private String profilePicture = "";

    public StudentProfile() {}

    public StudentProfile(User user, String studentId, String hostelBlock, String roomNumber, String profilePicture) {
        this.user = user;
        this.studentId = studentId;
        this.hostelBlock = hostelBlock;
        this.roomNumber = roomNumber;
        this.profilePicture = profilePicture;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getHostelBlock() { return hostelBlock; }
    public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}

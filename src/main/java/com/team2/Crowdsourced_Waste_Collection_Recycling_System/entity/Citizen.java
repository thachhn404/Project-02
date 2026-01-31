package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table citizens
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "citizens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Citizen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "ward", length = 100)
    private String ward;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "total_points")
    private Integer totalPoints;

    @Column(name = "total_reports")
    private Integer totalReports;

    @Column(name = "valid_reports")
    private Integer validReports;
}

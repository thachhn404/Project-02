package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table collectors
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "collectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Collector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private Enterprise enterprise;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "full_name", length = 255)
    @Nationalized
    private String fullName;

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;

    @Column(name = "vehicle_plate", length = 20)
    private String vehiclePlate;

    @Column(name = "status", length = 20)
    private CollectorStatus status;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "violation_count")
    private Integer violationCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

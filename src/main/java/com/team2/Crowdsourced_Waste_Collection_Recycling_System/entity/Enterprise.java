package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table enterprise
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "enterprise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Enterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 255)
    @Nationalized
    private String name;

    @Column(name = "address", length = 500)
    @Nationalized
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Lob
    @Column(name = "supported_waste_type_codes", columnDefinition = "NVARCHAR(MAX)")
    private String supportedWasteTypeCodes;

    @Lob
    @Column(name = "service_wards", columnDefinition = "NVARCHAR(MAX)")
    private String serviceWards;

    @Lob
    @Column(name = "service_cities", columnDefinition = "NVARCHAR(MAX)")
    private String serviceCities;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

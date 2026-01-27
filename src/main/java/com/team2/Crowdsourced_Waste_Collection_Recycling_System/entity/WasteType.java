package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waste_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WasteType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "base_points")
    private Integer basePoints;

    @Column(name = "is_recyclable")
    private Boolean isRecyclable;

    @Column(name = "handling_instructions", length = 1000)
    private String handlingInstructions;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (basePoints == null) {
            basePoints = 0;
        }
        if (isRecyclable == null) {
            isRecyclable = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

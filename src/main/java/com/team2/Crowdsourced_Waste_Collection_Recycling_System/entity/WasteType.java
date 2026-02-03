package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waste_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WasteType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false, length = 20)
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
}

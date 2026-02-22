package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "waste_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WasteCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100, unique = true)
    @Nationalized
    private String name;

    @Column(name = "description", length = 500)
    @Nationalized
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", length = 20)
    private WasteUnit unit;

    @Column(name = "point_per_unit", precision = 19, scale = 4)
    private BigDecimal pointPerUnit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

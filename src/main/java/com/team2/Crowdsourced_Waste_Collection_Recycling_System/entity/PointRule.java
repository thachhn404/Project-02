package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table point_rules
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private Enterprise enterprise;

    @Column(name = "rule_name", nullable = false, length = 255)
    @Nationalized
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 30)
    private String ruleType;

    @Column(name = "min_weight_kg", precision = 10, scale = 2)
    private BigDecimal minWeightKg;

    @Column(name = "max_weight_kg", precision = 10, scale = 2)
    private BigDecimal maxWeightKg;

    @Column(name = "min_quality_rating")
    private Integer minQualityRating;

    @Column(name = "max_processing_hours")
    private Integer maxProcessingHours;

    @Column(name = "base_points", nullable = false)
    private Integer basePoints;

    @Column(name = "multiplier", precision = 3, scale = 2)
    private BigDecimal multiplier;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


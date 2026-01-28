package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private RecyclingEnterprise enterprise;
    
    @Column(name = "rule_name", nullable = false)
    private String ruleName;
    
    @Column(name = "rule_type", nullable = false, length = 30)
    private String ruleType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waste_type_id")
    private WasteType wasteType;
    
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
    private BigDecimal multiplier = new BigDecimal("1.00");
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to")
    private LocalDateTime validTo;
    
    @Column(name = "priority")
    private Integer priority = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

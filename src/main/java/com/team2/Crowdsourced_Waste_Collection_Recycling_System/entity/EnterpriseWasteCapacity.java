package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table enterprise_waste_capacity
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "enterprise_waste_capacity",
        uniqueConstraints = @UniqueConstraint(name = "uq_enterprise_waste", columnNames = {"enterprise_id", "waste_type_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseWasteCapacity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private RecyclingEnterprise enterprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waste_type_id", nullable = false)
    private WasteType wasteType;

    @Column(name = "daily_capacity_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyCapacityKg;

    @Column(name = "current_load_kg", precision = 10, scale = 2)
    private BigDecimal currentLoadKg;

    @Column(name = "price_per_kg", precision = 10, scale = 2)
    private BigDecimal pricePerKg;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

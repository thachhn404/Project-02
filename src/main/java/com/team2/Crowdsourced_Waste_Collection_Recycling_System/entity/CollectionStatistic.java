package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table collection_statistics
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waste_type_id", nullable = false)
    private WasteType wasteType;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "total_reports")
    private Integer totalReports;

    @Column(name = "total_collections")
    private Integer totalCollections;

    @Column(name = "total_weight_kg", precision = 12, scale = 2)
    private BigDecimal totalWeightKg;

    @Column(name = "total_points_awarded")
    private Integer totalPointsAwarded;

    @Column(name = "avg_collection_time_hours", precision = 5, scale = 2)
    private BigDecimal avgCollectionTimeHours;

    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


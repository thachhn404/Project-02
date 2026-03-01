package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "waste_report_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WasteReportItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private WasteReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waste_category_id", nullable = false)
    private WasteCategory wasteCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_snapshot", length = 20)
    private WasteUnit unitSnapshot;

    @Column(name = "quantity", precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

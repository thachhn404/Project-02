package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collector_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectorReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_request_id", nullable = false)
    private CollectionRequest collectionRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collector_id", nullable = false)
    private Collector collector;

    @Column(name = "status", nullable = false, length = 20)
    private CollectorReportStatus status;

    @Column(name = "collector_note", length = 1000)
    private String collectorNote;

    @Column(name = "actual_weight")
    private BigDecimal actualWeight;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "collectorReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectorReportImage> images = new ArrayList<>();
}

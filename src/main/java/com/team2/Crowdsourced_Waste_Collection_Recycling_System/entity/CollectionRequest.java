package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table collection_requests
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"report", "enterprise", "collector"}) // Ngăn log / debug / exception leak data
public class CollectionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "request_code", nullable = false, unique = true, length = 20)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private WasteReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private Enterprise enterprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id")
    private Collector collector;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Column(name = "actual_weight_kg", precision = 10, scale = 2)
    private BigDecimal actualWeightKg;

    @Lob
    @Column(name = "collection_images", columnDefinition = "NVARCHAR(MAX)")
    private String collectionImages;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "collection_note", length = 500)
    private String collectionNote;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

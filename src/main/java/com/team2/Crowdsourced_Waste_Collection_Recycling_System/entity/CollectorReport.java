package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
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

    @Column(name = "report_code", unique = true, length = 20)
    private String reportCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_request_id", nullable = false)
    private CollectionRequest collectionRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collector_id", nullable = false)
    private Collector collector;

    @Column(name = "status", nullable = false, length = 20)
    private CollectorReportStatus status;

    @Column(name = "collector_note", length = 1000)
    @Nationalized
    private String collectorNote;

    @Column(name = "total_point")
    private Integer totalPoint;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setCollectorNote(String collectorNote) {
        this.collectorNote = collectorNote;
    }
    public String getCollectorNote() {
        return collectorNote;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }
    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table collection_tracking
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionTracking {
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

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "note", length = 500)
    @Nationalized
    private String note;

    @Lob
    @Column(name = "images", columnDefinition = "NVARCHAR(MAX)")
    private String images;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


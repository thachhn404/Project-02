package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_request_id", nullable = false)
    private CollectionRequest collectionRequest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id", nullable = false)
    private Collector collector;
    
    @Column(name = "action", nullable = false, length = 50)
    private String action;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "note", length = 500)
    private String note;
    
    @Column(name = "images", columnDefinition = "NVARCHAR(MAX)")
    private String images;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

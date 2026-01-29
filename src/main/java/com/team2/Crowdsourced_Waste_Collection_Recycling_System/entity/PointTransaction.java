package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table point_transactions
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

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private WasteReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_request_id")
    private CollectionRequest collectionRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private PointRule rule;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


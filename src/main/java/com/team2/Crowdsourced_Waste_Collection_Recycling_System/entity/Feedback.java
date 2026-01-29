package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table feedbacks
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

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "feedback_code", nullable = false, unique = true, length = 20)
    private String feedbackCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_request_id")
    private CollectionRequest collectionRequest;

    @Column(name = "feedback_type", nullable = false, length = 20)
    private String feedbackType;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Lob
    @Column(name = "images", columnDefinition = "NVARCHAR(MAX)")
    private String images;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "status", length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Lob
    @Column(name = "resolution", columnDefinition = "NVARCHAR(MAX)")
    private String resolution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


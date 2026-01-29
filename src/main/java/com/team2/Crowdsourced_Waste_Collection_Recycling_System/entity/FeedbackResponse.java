package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table feedback_responses
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
@Table(name = "feedback_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responder_id", nullable = false)
    private User responder;

    @Lob
    @Column(name = "response", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String response;

    @Column(name = "is_internal")
    private Boolean isInternal;

    @Lob
    @Column(name = "attachments", columnDefinition = "NVARCHAR(MAX)")
    private String attachments;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


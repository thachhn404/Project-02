package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id", nullable = false)
    private User responder;
    
    @Column(name = "response", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String response;
    
    @Column(name = "is_internal")
    private Boolean isInternal = false;
    
    @Column(name = "attachments", columnDefinition = "NVARCHAR(MAX)")
    private String attachments;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
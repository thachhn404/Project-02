package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.FeedbackResponse;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, Integer> {
    
    List<FeedbackResponse> findByFeedbackId(Integer feedbackId);
    
    List<FeedbackResponse> findByResponderId(Integer responderId);
    
    List<FeedbackResponse> findByIsInternal(Boolean isInternal);
    
    @Query("SELECT fr FROM FeedbackResponse fr WHERE fr.feedback.id = :feedbackId ORDER BY fr.createdAt ASC")
    List<FeedbackResponse> findByFeedbackIdOrderByCreatedAtAsc(@Param("feedbackId") Integer feedbackId);
    
    @Query("SELECT fr FROM FeedbackResponse fr WHERE fr.feedback.id = :feedbackId AND fr.isInternal = :isInternal")
    List<FeedbackResponse> findByFeedbackIdAndIsInternal(
        @Param("feedbackId") Integer feedbackId,
        @Param("isInternal") Boolean isInternal
    );
    
    @Query("SELECT fr FROM FeedbackResponse fr WHERE fr.responder.id = :responderId AND fr.createdAt BETWEEN :startDate AND :endDate")
    List<FeedbackResponse> findByResponderIdAndDateRange(
        @Param("responderId") Integer responderId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(fr) FROM FeedbackResponse fr WHERE fr.feedback.id = :feedbackId")
    Long countByFeedbackId(@Param("feedbackId") Integer feedbackId);
    
    @Query("SELECT COUNT(fr) FROM FeedbackResponse fr WHERE fr.feedback.id = :feedbackId AND fr.isInternal = false")
    Long countPublicResponsesByFeedbackId(@Param("feedbackId") Integer feedbackId);
    
    @Query("SELECT fr FROM FeedbackResponse fr WHERE fr.feedback.id = :feedbackId ORDER BY fr.createdAt DESC LIMIT 1")
    FeedbackResponse findLatestResponseByFeedbackId(@Param("feedbackId") Integer feedbackId);
}


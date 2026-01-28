package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Feedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    
    Optional<Feedback> findByFeedbackCode(String feedbackCode);
    
    List<Feedback> findByCitizenId(Integer citizenId);
    
    List<Feedback> findByStatus(String status);
    
    List<Feedback> findByFeedbackType(String feedbackType);
    
    List<Feedback> findBySeverity(String severity);
    
    List<Feedback> findByCollectionRequestId(Integer collectionRequestId);
    
    List<Feedback> findByAssignedToId(Integer assignedToId);
    
    @Query("SELECT f FROM Feedback f WHERE f.citizen.id = :citizenId ORDER BY f.createdAt DESC")
    List<Feedback> findByCitizenIdOrderByCreatedAtDesc(@Param("citizenId") Integer citizenId);
    
    @Query("SELECT f FROM Feedback f WHERE f.status = :status AND f.severity = :severity")
    List<Feedback> findByStatusAndSeverity(
        @Param("status") String status,
        @Param("severity") String severity
    );
    
    @Query("SELECT f FROM Feedback f WHERE f.assignedTo.id = :userId AND f.status = :status")
    List<Feedback> findByAssignedToIdAndStatus(
        @Param("userId") Integer userId,
        @Param("status") String status
    );
    
    @Query("SELECT f FROM Feedback f WHERE f.status = 'pending' AND f.assignedTo IS NULL")
    List<Feedback> findUnassignedFeedbacks();
    
    @Query("SELECT f FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    List<Feedback> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT f FROM Feedback f WHERE f.feedbackType = :type AND f.createdAt BETWEEN :startDate AND :endDate")
    List<Feedback> findByFeedbackTypeAndDateRange(
        @Param("type") String type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.assignedTo.id = :userId AND f.status != 'resolved'")
    Long countPendingByAssignedTo(@Param("userId") Integer userId);
    
    @Query("SELECT f FROM Feedback f WHERE f.severity = 'high' AND f.status IN ('pending', 'in_progress') ORDER BY f.createdAt ASC")
    List<Feedback> findUrgentFeedbacks();
}


package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.feedback;

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
    
    List<Feedback> findByCollectionRequestId(Integer collectionRequestId);

    
    @Query("SELECT f FROM Feedback f WHERE f.collectionRequest.enterprise.id = :enterpriseId ORDER BY f.createdAt DESC")
    List<Feedback> findByEnterpriseId(@Param("enterpriseId") Integer enterpriseId);
    
    @Query("SELECT f FROM Feedback f WHERE f.citizen.id = :citizenId ORDER BY f.createdAt DESC")
    List<Feedback> findByCitizenIdOrderByCreatedAtDesc(@Param("citizenId") Integer citizenId);
    
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
}


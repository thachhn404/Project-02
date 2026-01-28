package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, Integer> {
    
    Optional<CollectionRequest> findByRequestCode(String requestCode);
    
    List<CollectionRequest> findByStatus(String status);
    
    List<CollectionRequest> findByPriority(String priority);
    
    List<CollectionRequest> findByEnterpriseId(Integer enterpriseId);
    
    List<CollectionRequest> findByCollectorId(Integer collectorId);
    
    List<CollectionRequest> findByWasteReportId(Integer reportId);
    
    @Query("SELECT cr FROM CollectionRequest cr WHERE cr.status = :status AND cr.priority = :priority")
    List<CollectionRequest> findByStatusAndPriority(@Param("status") String status, @Param("priority") String priority);
    
    @Query("SELECT cr FROM CollectionRequest cr WHERE cr.collector.id = :collectorId AND cr.status = :status")
    List<CollectionRequest> findByCollectorIdAndStatus(@Param("collectorId") Integer collectorId, @Param("status") String status);
    
    @Query("SELECT cr FROM CollectionRequest cr WHERE cr.enterprise.id = :enterpriseId AND cr.createdAt BETWEEN :startDate AND :endDate")
    List<CollectionRequest> findByEnterpriseIdAndDateRange(
        @Param("enterpriseId") Integer enterpriseId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(cr) FROM CollectionRequest cr WHERE cr.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT cr FROM CollectionRequest cr WHERE cr.assignedAt IS NULL AND cr.status = 'pending'")
    List<CollectionRequest> findUnassignedRequests();
    
    @Query("SELECT cr FROM CollectionRequest cr WHERE cr.estimatedArrival < :deadline AND cr.status NOT IN ('completed', 'cancelled')")
    List<CollectionRequest> findOverdueRequests(@Param("deadline") LocalDateTime deadline);
}

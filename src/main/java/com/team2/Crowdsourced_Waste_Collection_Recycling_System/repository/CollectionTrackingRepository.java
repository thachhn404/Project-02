package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CollectionTrackingRepository extends JpaRepository<CollectionTracking, Integer> {
    
    List<CollectionTracking> findByCollectionRequestId(Integer collectionRequestId);
    
    List<CollectionTracking> findByCollectorId(Integer collectorId);
    
    List<CollectionTracking> findByAction(String action);
    
    @Query("SELECT ct FROM CollectionTracking ct WHERE ct.collectionRequest.id = :requestId ORDER BY ct.createdAt DESC")
    List<CollectionTracking> findByCollectionRequestIdOrderByCreatedAtDesc(@Param("requestId") Integer requestId);
    
    @Query("SELECT ct FROM CollectionTracking ct WHERE ct.collector.id = :collectorId AND ct.createdAt BETWEEN :startDate AND :endDate")
    List<CollectionTracking> findByCollectorIdAndDateRange(
        @Param("collectorId") Integer collectorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT ct FROM CollectionTracking ct WHERE ct.collectionRequest.id = :requestId AND ct.action = :action")
    List<CollectionTracking> findByCollectionRequestIdAndAction(
        @Param("requestId") Integer requestId,
        @Param("action") String action
    );
    
    @Query("SELECT ct FROM CollectionTracking ct WHERE ct.latitude IS NOT NULL AND ct.longitude IS NOT NULL AND ct.collectionRequest.id = :requestId")
    List<CollectionTracking> findLocationTrackingByRequestId(@Param("requestId") Integer requestId);
    
    @Query("SELECT ct FROM CollectionTracking ct WHERE ct.collector.id = :collectorId ORDER BY ct.createdAt DESC")
    List<CollectionTracking> findLatestTrackingByCollector(@Param("collectorId") Integer collectorId);
}

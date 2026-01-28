package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointTransaction;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Integer> {
    
    List<PointTransaction> findByCitizenId(Integer citizenId);
    
    List<PointTransaction> findByTransactionType(String transactionType);
    
    List<PointTransaction> findByCollectionRequestId(Integer collectionRequestId);
    
    List<PointTransaction> findByWasteReportId(Integer wasteReportId);
    
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.citizen.id = :citizenId ORDER BY pt.createdAt DESC")
    List<PointTransaction> findByCitizenIdOrderByCreatedAtDesc(@Param("citizenId") Integer citizenId);
    
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.citizen.id = :citizenId AND pt.transactionType = :type")
    List<PointTransaction> findByCitizenIdAndTransactionType(
        @Param("citizenId") Integer citizenId,
        @Param("type") String type
    );
    
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.citizen.id = :citizenId AND pt.createdAt BETWEEN :startDate AND :endDate")
    List<PointTransaction> findByCitizenIdAndDateRange(
        @Param("citizenId") Integer citizenId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.citizen.id = :citizenId")
    Integer getTotalPointsByCitizenId(@Param("citizenId") Integer citizenId);
    
    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.citizen.id = :citizenId AND pt.transactionType = :type")
    Integer getTotalPointsByCitizenIdAndType(
        @Param("citizenId") Integer citizenId,
        @Param("type") String type
    );
    
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.citizen.id = :citizenId ORDER BY pt.createdAt DESC LIMIT 1")
    PointTransaction findLatestTransactionByCitizenId(@Param("citizenId") Integer citizenId);
    
    @Query("SELECT COUNT(pt) FROM PointTransaction pt WHERE pt.citizen.id = :citizenId AND pt.createdAt BETWEEN :startDate AND :endDate")
    Long countTransactionsByCitizenIdAndDateRange(
        @Param("citizenId") Integer citizenId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.pointRule.id = :ruleId")
    List<PointTransaction> findByPointRuleId(@Param("ruleId") Integer ruleId);
}


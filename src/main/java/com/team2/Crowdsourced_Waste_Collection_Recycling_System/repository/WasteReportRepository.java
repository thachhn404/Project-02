package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WasteReportRepository extends JpaRepository<WasteReport, Integer> {
    
    Optional<WasteReport> findByReportCode(String reportCode);
    
    List<WasteReport> findByCitizenId(Integer citizenId);
    
    List<WasteReport> findByStatus(String status);
    
    List<WasteReport> findByWasteTypeId(Integer wasteTypeId);
    
    List<WasteReport> findByDistrict(String district);
    
    List<WasteReport> findByCity(String city);
    
    @Query("SELECT w FROM WasteReport w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<WasteReport> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT w FROM WasteReport w WHERE w.status = :status AND w.isValid IS NULL")
    List<WasteReport> findPendingValidation(@Param("status") String status);
    
    @Query("SELECT COUNT(w) FROM WasteReport w WHERE w.citizen.id = :citizenId")
    long countByCitizenId(@Param("citizenId") Integer citizenId);
}
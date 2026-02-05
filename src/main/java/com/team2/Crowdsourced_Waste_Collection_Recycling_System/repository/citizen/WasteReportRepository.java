package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.citizen;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WasteReportRepository extends JpaRepository<WasteReport, Integer> {
    List<WasteReport> findByCitizen_Id(Integer citizenId);
    List<WasteReport> findByStatus(String status);
    long countByCitizen_IdAndCreatedAtBetween(Integer citizenId, LocalDateTime start, LocalDateTime end);
    Optional<WasteReport> findByReportCode(String reportCode);
}

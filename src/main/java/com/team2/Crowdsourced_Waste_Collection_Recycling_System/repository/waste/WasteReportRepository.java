package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WasteReportRepository extends JpaRepository<WasteReport, Integer> {
    List<WasteReport> findByCitizen_Id(Integer citizenId);

    List<WasteReport> findByStatus(String status);

    List<WasteReport> findByStatus(WasteReportStatus status);

    long countByCitizen_IdAndCreatedAtBetween(Integer citizenId, LocalDateTime start, LocalDateTime end);

    Optional<WasteReport> findByReportCode(String reportCode);

    @Query("""
            SELECT COUNT(wr)
            FROM WasteReport wr
            WHERE wr.citizen.id = :citizenId
              AND wr.createdAt BETWEEN :from AND :to
              AND ABS(wr.latitude - :lat) <= :latDelta
              AND ABS(wr.longitude - :lng) <= :lngDelta
            """)
    long countRecentNearDuplicate(
            @Param("citizenId") Integer citizenId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("lat") BigDecimal lat,
            @Param("lng") BigDecimal lng,
            @Param("latDelta") BigDecimal latDelta,
            @Param("lngDelta") BigDecimal lngDelta);
}

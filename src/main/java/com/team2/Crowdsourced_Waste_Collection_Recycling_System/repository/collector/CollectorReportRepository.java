package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectorReportRepository extends JpaRepository<CollectorReport, Integer> {
    Optional<CollectorReport> findByCollectionRequestId(Integer collectionRequestId);
    List<CollectorReport> findByCollectorIdOrderByCreatedAtDesc(Integer collectorId);
    List<CollectorReport> findByCollectorIdAndStatus(Integer collectorId, CollectorReportStatus status);

}

package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectorReportRepository extends JpaRepository<CollectorReport, Integer> {
    Optional<CollectorReport> findByCollectionRequest_Id(Integer requestId);
    Optional<CollectorReport> findByCollectionRequestId(Integer requestId);
    List<CollectorReport> findByCollector_Id(Integer collectorId);
}

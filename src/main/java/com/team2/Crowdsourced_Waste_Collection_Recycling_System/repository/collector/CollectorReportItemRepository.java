package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectorReportItemRepository extends JpaRepository<CollectorReportItem, Integer> {
    List<CollectorReportItem> findByCollectorReport_Id(Integer collectorReportId);
}

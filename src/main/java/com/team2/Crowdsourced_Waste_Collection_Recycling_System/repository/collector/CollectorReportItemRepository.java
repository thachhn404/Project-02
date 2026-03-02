package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectorReportItemRepository extends JpaRepository<CollectorReportItem, Integer> {
    List<CollectorReportItem> findByCollectorReport_Id(Integer collectorReportId);

    @Query("select i from CollectorReportItem i join fetch i.wasteCategory where i.collectorReport.id = :reportId")
    List<CollectorReportItem> findWithCategoryByCollectorReportId(@Param("reportId") Integer reportId);

    @Query("select i from CollectorReportItem i join fetch i.wasteCategory where i.collectorReport.id in :reportIds")
    List<CollectorReportItem> findWithCategoryByCollectorReportIdIn(@Param("reportIds") List<Integer> reportIds);
}

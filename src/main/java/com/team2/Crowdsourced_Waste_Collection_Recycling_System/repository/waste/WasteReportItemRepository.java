package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteReportItemRepository extends JpaRepository<WasteReportItem, Integer> {
    List<WasteReportItem> findByReport_Id(Integer reportId);

    @Query("select i from WasteReportItem i join fetch i.wasteCategory where i.report.id = :reportId")
    List<WasteReportItem> findWithCategoryByReportId(@Param("reportId") Integer reportId);

    @Query("select i from WasteReportItem i join fetch i.wasteCategory where i.report.id in :reportIds")
    List<WasteReportItem> findWithCategoryByReportIdIn(@Param("reportIds") List<Integer> reportIds);

    void deleteByReport_Id(Integer reportId);
}

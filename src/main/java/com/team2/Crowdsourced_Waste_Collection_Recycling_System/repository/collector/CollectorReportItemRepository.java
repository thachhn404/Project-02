package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CollectorReportItemRepository extends JpaRepository<CollectorReportItem, Integer> {
    interface AdminCategoryCollectedWeightView {
        Integer getCategoryId();

        String getCategoryName();

        BigDecimal getTotalWeightKg();
    }

    List<CollectorReportItem> findByCollectorReport_Id(Integer collectorReportId);

    @Query("select i from CollectorReportItem i join fetch i.wasteCategory where i.collectorReport.id = :reportId")
    List<CollectorReportItem> findWithCategoryByCollectorReportId(@Param("reportId") Integer reportId);

    @Query("select i from CollectorReportItem i join fetch i.wasteCategory where i.collectorReport.id in :reportIds")
    List<CollectorReportItem> findWithCategoryByCollectorReportIdIn(@Param("reportIds") List<Integer> reportIds);

    @Query(value = """
            SELECT
                wc.id AS categoryId,
                wc.name AS categoryName,
                COALESCE(SUM(cri.quantity), 0) AS totalWeightKg
            FROM collector_report_items cri
            JOIN collector_reports crp ON crp.id = cri.collector_report_id
            JOIN collection_requests cr ON cr.id = crp.collection_request_id
            JOIN waste_categories wc ON wc.id = cri.waste_category_id
            WHERE cr.status = 'completed'
              AND cri.unit_snapshot = 'KG'
            GROUP BY wc.id, wc.name
            ORDER BY totalWeightKg DESC, wc.id ASC
            """, nativeQuery = true)
    List<AdminCategoryCollectedWeightView> sumGlobalCollectedWeightByCategory();

    @Query(value = """
            SELECT
                wc.id AS categoryId,
                wc.name AS categoryName,
                COALESCE(SUM(cri.quantity), 0) AS totalWeightKg
            FROM collector_report_items cri
            JOIN collector_reports crp ON crp.id = cri.collector_report_id
            JOIN collection_requests cr ON cr.id = crp.collection_request_id
            JOIN waste_categories wc ON wc.id = cri.waste_category_id
            WHERE cr.status = 'completed'
              AND cri.unit_snapshot = 'KG'
              AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
              AND YEAR(COALESCE(cr.completed_at, cr.collected_at)) = :year
            GROUP BY wc.id, wc.name
            ORDER BY totalWeightKg DESC, wc.id ASC
            """, nativeQuery = true)
    List<AdminCategoryCollectedWeightView> sumCollectedWeightByCategoryForYear(@Param("year") Integer year);

    @Query("""
        SELECT c.name, SUM(i.quantity) 
        FROM CollectorReportItem i 
        JOIN i.wasteCategory c 
        JOIN i.collectorReport cr 
        JOIN cr.collectionRequest req 
        JOIN req.report wr 
        WHERE wr.citizen.id = :citizenId 
        GROUP BY c.name
    """)
    List<Object[]> sumWeightByWasteTypeForCitizen(@Param("citizenId") Integer citizenId);

    @Query("""
        SELECT c.name, SUM(i.quantity) 
        FROM CollectorReportItem i 
        JOIN i.wasteCategory c 
        JOIN i.collectorReport cr 
        JOIN cr.collectionRequest req 
        WHERE req.enterprise.id = :enterpriseId 
        GROUP BY c.name
    """)
    List<Object[]> sumWeightByWasteTypeForEnterprise(@Param("enterpriseId") Integer enterpriseId);

    @Query("""
        SELECT c.name, SUM(i.quantity) 
        FROM CollectorReportItem i 
        JOIN i.wasteCategory c 
        JOIN i.collectorReport cr 
        JOIN cr.collectionRequest req 
        WHERE req.collector.id = :collectorId 
        GROUP BY c.name
    """)
    List<Object[]> sumWeightByWasteTypeForCollector(@Param("collectorId") Integer collectorId);
}

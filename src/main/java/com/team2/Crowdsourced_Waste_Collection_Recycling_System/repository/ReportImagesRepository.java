package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportImagesRepository extends JpaRepository<ReportImages, Integer> {
    
    List<ReportImages> findByReportId(Integer reportId);
    
    List<ReportImages> findByImageType(String imageType);
    
    @Query("SELECT r FROM ReportImages r WHERE r.report.id = :reportId AND r.imageType = :imageType")
    List<ReportImages> findByReportIdAndImageType(@Param("reportId") Integer reportId, 
                                                    @Param("imageType") String imageType);
    
    @Query("SELECT COUNT(r) FROM ReportImages r WHERE r.report.id = :reportId")
    long countByReportId(@Param("reportId") Integer reportId);
    
    void deleteByReportId(Integer reportId);
}
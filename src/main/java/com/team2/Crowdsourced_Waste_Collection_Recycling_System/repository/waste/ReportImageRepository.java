package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportImageRepository extends JpaRepository<ReportImage, Integer> {
    List<ReportImage> findByReport_Id(Integer reportId);

    void deleteByReport_Id(Integer reportId);
}

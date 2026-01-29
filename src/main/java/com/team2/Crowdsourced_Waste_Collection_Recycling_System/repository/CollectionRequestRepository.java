package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, Integer> {
    List<CollectionRequest> findByCollector_Id(Integer collectorId);
    List<CollectionRequest> findByEnterprise_Id(Integer enterpriseId);
    List<CollectionRequest> findByStatus(String status);
}

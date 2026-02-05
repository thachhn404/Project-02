package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;

@Repository
public interface CollectionTrackingRepository extends JpaRepository<CollectionTracking, Integer> {
    boolean existsByCollectionRequest_IdAndAction(Integer collectionRequestId, String action);
}

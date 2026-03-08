package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;

import java.util.Optional;

@Repository
public interface CollectionTrackingRepository extends JpaRepository<CollectionTracking, Integer> {
    boolean existsByCollectionRequest_IdAndAction(Integer collectionRequestId, String action);

    Optional<CollectionTracking> findFirstByCollectionRequest_IdAndActionOrderByCreatedAtDesc(
            Integer collectionRequestId,
            String action);
}

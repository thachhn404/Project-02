package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequestImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRequestImageRepository extends JpaRepository<CollectionRequestImage, Integer> {
    List<CollectionRequestImage> findByCollectionRequest_Id(Integer collectionRequestId);
}


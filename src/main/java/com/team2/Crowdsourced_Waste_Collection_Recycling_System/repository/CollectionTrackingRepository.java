package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CollectionTrackingRepository extends JpaRepository<CollectionTracking, Integer> {

}

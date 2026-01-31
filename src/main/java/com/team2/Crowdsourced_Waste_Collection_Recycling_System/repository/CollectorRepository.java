package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectorRepository extends JpaRepository<Collector, Integer> {
    boolean existsByIdAndEnterpriseIdAndStatus(Integer id, Integer enterpriseId, String status);

    Optional<Collector> findByUserId(Integer userId);
}

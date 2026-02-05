package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectorRepository extends JpaRepository<Collector, Integer> {
    boolean existsByIdAndEnterprise_IdAndStatus(Integer id, Integer enterpriseId, CollectorStatus status);

    Optional<Collector> findByUserId(Integer userId);

    List<Collector> findByEnterprise_IdOrderByCreatedAtDesc(Integer enterpriseId);

    List<Collector> findByEnterprise_IdAndStatusOrderByCreatedAtDesc(Integer enterpriseId, CollectorStatus status);
}

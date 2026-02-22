package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WasteCategoryRepository extends JpaRepository<WasteCategory, Integer> {
    Optional<WasteCategory> findByName(String name);

    Optional<WasteCategory> findByNameIgnoreCase(String name);
}


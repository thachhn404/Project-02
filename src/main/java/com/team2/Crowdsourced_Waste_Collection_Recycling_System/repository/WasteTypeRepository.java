package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WasteTypeRepository extends JpaRepository<WasteType, Integer> {
    
    Optional<WasteType> findByCode(String code);
    
    Optional<WasteType> findByName(String name);
    
    List<WasteType> findByCategory(String category);
    
    List<WasteType> findByIsRecyclable(Boolean isRecyclable);
    
    @Query("SELECT w FROM WasteType w WHERE w.isRecyclable = true ORDER BY w.basePoints DESC")
    List<WasteType> findAllRecyclableOrderByPoints();
    
    @Query("SELECT w FROM WasteType w WHERE w.category = :category ORDER BY w.basePoints DESC")
    List<WasteType> findByCategoryOrderByPoints(@Param("category") String category);
}
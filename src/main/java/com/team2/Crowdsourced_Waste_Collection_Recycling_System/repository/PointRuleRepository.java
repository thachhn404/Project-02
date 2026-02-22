package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PointRuleRepository extends JpaRepository<PointRule, Integer> {
    
    List<PointRule> findByEnterpriseId(Integer enterpriseId);
    
    List<PointRule> findByRuleType(String ruleType);
    
    List<PointRule> findByIsActive(Boolean isActive);
    
    @Query("SELECT pr FROM PointRule pr WHERE pr.enterprise.id = :enterpriseId AND pr.isActive = true")
    List<PointRule> findActiveRulesByEnterpriseId(@Param("enterpriseId") Integer enterpriseId);
    
    @Query("SELECT pr FROM PointRule pr WHERE pr.enterprise.id = :enterpriseId AND pr.ruleType = :ruleType AND pr.isActive = true")
    List<PointRule> findActiveRulesByEnterpriseAndType(
        @Param("enterpriseId") Integer enterpriseId,
        @Param("ruleType") String ruleType
    );
    
    
    @Query("SELECT pr FROM PointRule pr WHERE pr.validTo < :currentDate AND pr.isActive = true")
    List<PointRule> findExpiredActiveRules(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT pr FROM PointRule pr WHERE pr.enterprise.id = :enterpriseId ORDER BY pr.priority DESC, pr.createdAt DESC")
    List<PointRule> findByEnterpriseIdOrderByPriority(@Param("enterpriseId") Integer enterpriseId);

    Optional<PointRule> findByEnterpriseIdAndRuleName(Integer enterpriseId, String ruleName);
}


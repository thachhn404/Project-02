package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, Long> {
    //Check collection request
    //dung enterprise thì status = accept
    boolean existsByIdAndEnterpriseIdAndStatus(Long id, Long enterpriseId, String status);
    /**
     * Gán collector cho request
     *Native query để kiểm soát field update
     */
    @Modifying
    @Query(value = """
        UPDATE collection_requests
        SET collector_id = :collectorId,
            status = 'assigned',
            assigned_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :requestId
          AND enterprise_id = :enterpriseId
          AND status = 'accepted'
    """, nativeQuery = true)
    int assignCollector(
            @Param("requestId") Long requestId,
            @Param("collectorId") Long collectorId,
            @Param("enterpriseId") Long enterpriseId
    );

}

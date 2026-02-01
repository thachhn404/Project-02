package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, Integer> {
    /**
     * Projection trả về danh sách task của collector (ẩn dữ liệu citizen).
     */
    interface CollectorTaskView {
        Integer getId();

        String getRequestCode();

        String getStatus();

        String getPriority();

        java.time.LocalDateTime getAssignedAt();

        java.time.LocalDateTime getEstimatedArrival();

        java.math.BigDecimal getDistanceKm();

        java.time.LocalDateTime getCreatedAt();

        java.time.LocalDateTime getUpdatedAt();
    }

    boolean existsByIdAndEnterpriseIdAndStatus(Integer id, Integer enterpriseId, String status);

    /**
     * Gán collector cho request theo hướng atomic (native query).
     * Chỉ cho phép gán khi request thuộc enterprise và đang ở trạng thái pending.
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
          AND status = 'pending'
    """, nativeQuery = true)
    int assignCollector(
            @Param("requestId") Integer requestId,
            @Param("collectorId") Integer collectorId,
            @Param("enterpriseId") Integer enterpriseId
    );

    /**
     * Lấy danh sách task của collector (tất cả trạng thái).
     */
    @Query(value = """
        SELECT
            cr.id AS id,
            cr.request_code AS requestCode,
            cr.status AS status,
            cr.priority AS priority,
            cr.assigned_at AS assignedAt,
            cr.estimated_arrival AS estimatedArrival,
            cr.distance_km AS distanceKm,
            cr.created_at AS createdAt,
            cr.updated_at AS updatedAt
        FROM collection_requests cr
        WHERE cr.collector_id = :collectorId
        ORDER BY
            CASE WHEN cr.assigned_at IS NULL THEN 1 ELSE 0 END,
            cr.assigned_at DESC,
            cr.id DESC
    """, nativeQuery = true)
    List<CollectorTaskView> findTasksForCollector(@Param("collectorId") Integer collectorId);

    /**
     * Lấy danh sách task của collector theo trạng thái.
     */
    @Query(value = """
        SELECT
            cr.id AS id,
            cr.request_code AS requestCode,
            cr.status AS status,
            cr.priority AS priority,
            cr.assigned_at AS assignedAt,
            cr.estimated_arrival AS estimatedArrival,
            cr.distance_km AS distanceKm,
            cr.created_at AS createdAt,
            cr.updated_at AS updatedAt
        FROM collection_requests cr
        WHERE cr.collector_id = :collectorId
          AND cr.status = :status
        ORDER BY
            CASE WHEN cr.assigned_at IS NULL THEN 1 ELSE 0 END,
            cr.assigned_at DESC,
            cr.id DESC
    """, nativeQuery = true)
    List<CollectorTaskView> findTasksForCollectorByStatus(
            @Param("collectorId") Integer collectorId,
            @Param("status") String status
    );

    /**
     * Danh sách task mặc định cho Collector: chỉ hiển thị ASSIGNED và ON_THE_WAY.
     */
    @Query(value = """
        SELECT
            cr.id AS id,
            cr.request_code AS requestCode,
            cr.status AS status,
            cr.priority AS priority,
            cr.assigned_at AS assignedAt,
            cr.estimated_arrival AS estimatedArrival,
            cr.distance_km AS distanceKm,
            cr.created_at AS createdAt,
            cr.updated_at AS updatedAt
        FROM collection_requests cr
        WHERE cr.collector_id = :collectorId
          AND cr.status IN ('assigned', 'on_the_way')
        ORDER BY
            CASE WHEN cr.assigned_at IS NULL THEN 1 ELSE 0 END,
            cr.assigned_at DESC,
            cr.id DESC
    """, nativeQuery = true)
    List<CollectorTaskView> findActiveTasksForCollector(@Param("collectorId") Integer collectorId);

    /**
     * Tìm request theo id và collector đang được gán (phục vụ check ownership nhanh).
     */
    Optional<CollectionRequest> findByIdAndCollector_Id(Integer id, Integer collectorId);

    /**
     * Cập nhật trạng thái theo điều kiện (atomic) - dùng khi muốn update bằng query thay vì load entity.
     * Ví dụ: assigned -> on_the_way.
     */
    @Modifying
    @Query("""
        UPDATE CollectionRequest cr
        SET cr.status = :newStatus,
            cr.startedAt = :time,
            cr.updatedAt = :time
        WHERE cr.id = :id
          AND cr.collector.id = :collectorId
          AND cr.status = :currentStatus
    """)
    int updateStatusIfMatch(@Param("id") Integer id,
                            @Param("collectorId") Integer collectorId,
                            @Param("currentStatus") String currentStatus,
                            @Param("newStatus") String newStatus,
                            @Param("time") LocalDateTime time);

    /**
     * Từ chối nhiệm vụ theo hướng atomic:
     * - Chỉ khi request đang assigned và thuộc collector hiện tại
     * - Set status = accepted, lưu lý do, và unassign collector để enterprise gán lại
     */
    @Modifying
    @Query("update CollectionRequest cr " +
            "set cr.status = 'accepted'," +
            "cr.rejectionReason =:reason," +
            "cr.collector = null," +
            " cr.updatedAt = CURRENT_TIMESTAMP\n" +
            "        WHERE cr.id = :id\n" +
            "          AND cr.collector.id = :collectorId\n" +
            "          AND cr.status = 'assigned'")
    int rejectTask(
            @Param("id") Integer id,
            @Param("collectorId") Integer collectorId,
            @Param("reason") String reason
    );

    /**
     * Hoàn thành nhiệm vụ theo hướng atomic:
     * - Chỉ khi request đang on_the_way và thuộc collector hiện tại
     * - Set status = collected, set collectedAt
     */
    @Modifying
    @Query("""
        UPDATE CollectionRequest cr
        SET cr.status = 'collected',
            cr.collectedAt = :time,
            cr.updatedAt = :time
        WHERE cr.id = :id
          AND cr.collector.id = :collectorId
          AND cr.status = 'on_the_way'
    """)
    int completeTask(
            @Param("id") Integer id,
            @Param("collectorId") Integer collectorId,
            @Param("time") LocalDateTime time
    );
}

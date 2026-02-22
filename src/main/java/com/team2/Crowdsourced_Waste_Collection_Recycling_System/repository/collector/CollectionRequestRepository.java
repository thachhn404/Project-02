package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        java.time.LocalDateTime getAssignedAt();

        java.time.LocalDateTime getCreatedAt();

        java.time.LocalDateTime getUpdatedAt();
    }

    List<CollectionRequest> findByEnterprise_Id(Integer enterpriseId);

    Optional<CollectionRequest> findByRequestCode(String requestCode);

    boolean existsByIdAndEnterprise_IdAndStatus(Integer id, Integer enterpriseId, CollectionRequestStatus status);

    /**
     * Kiểm tra xem đã có CollectionRequest cho WasteReport này chưa
     */
    boolean existsByReport_Id(Integer reportId);

    Optional<CollectionRequest> findByReport_Id(Integer reportId);

    /**
     * Enterprise accept request: pending -> accepted_enterprise (chỉ khi chưa gán
     * collector).
     */
    @Modifying
    @Query(value = """
                UPDATE collection_requests
                SET status = 'accepted_enterprise',
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :requestId
                  AND enterprise_id = :enterpriseId
                  AND collector_id IS NULL
                  AND status = 'pending'
            """, nativeQuery = true)
    int acceptByEnterprise(
            @Param("requestId") Integer requestId,
            @Param("enterpriseId") Integer enterpriseId);

    @Modifying
    @Query(value = """
                UPDATE collection_requests
                SET status = 'accepted_enterprise',
                    updated_at = CURRENT_TIMESTAMP
                WHERE request_code = :requestCode
                  AND enterprise_id = :enterpriseId
                  AND collector_id IS NULL
                  AND status = 'pending'
            """, nativeQuery = true)
    int acceptByEnterpriseByRequestCode(
            @Param("requestCode") String requestCode,
            @Param("enterpriseId") Integer enterpriseId);

    /**
     * Chỉ cho phép gán khi request thuộc enterprise, chưa được gán collector, và
     * đang ở trạng thái accepted_enterprise.
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
                  AND collector_id IS NULL
                  AND (status = 'accepted_enterprise' OR status = 'pending')
            """, nativeQuery = true)
    int assignCollector(
            @Param("requestId") Integer requestId,
            @Param("collectorId") Integer collectorId,
            @Param("enterpriseId") Integer enterpriseId);

    @Modifying
    @Query(value = """
                UPDATE collection_requests
                SET collector_id = :collectorId,
                status = 'assigned',
                assigned_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
                WHERE request_code = :requestCode
                  AND enterprise_id = :enterpriseId
                  AND collector_id IS NULL
                  AND (status = 'accepted_enterprise' OR status = 'pending')
            """, nativeQuery = true)
    int assignCollectorByRequestCode(
            @Param("requestCode") String requestCode,
            @Param("collectorId") Integer collectorId,
            @Param("enterpriseId") Integer enterpriseId);

    @Query(value = """
                SELECT
                    cr.id AS id,
                    cr.request_code AS requestCode,
                    cr.status AS status,
                    cr.assigned_at AS assignedAt,
                    cr.created_at AS createdAt,
                    cr.updated_at AS updatedAt
                FROM collection_requests cr
                WHERE cr.collector_id = :collectorId
                ORDER BY
                    CASE WHEN cr.assigned_at IS NULL THEN 1 ELSE 0 END,
                    cr.assigned_at DESC,
                    cr.id DESC
            """, nativeQuery = true)
    Page<CollectorTaskView> findTasksForCollector(@Param("collectorId") Integer collectorId, Pageable pageable);

    /**
     * Lấy danh sách task của collector theo trạng thái.
     */
    @Query(value = """
                SELECT
                    cr.id AS id,
                    cr.request_code AS requestCode,
                    cr.status AS status,
                    cr.assigned_at AS assignedAt,
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
    Page<CollectorTaskView> findTasksForCollectorByStatus(
            @Param("collectorId") Integer collectorId,
            @Param("status") String status,
            Pageable pageable);

    /**
     * Danh sách task mặc định cho Collector: hiển thị ASSIGNED, ACCEPTED_COLLECTOR
     * và ON_THE_WAY
     */
    @Query(value = """
                SELECT
                    cr.id AS id,
                    cr.request_code AS requestCode,
                    cr.status AS status,
                    cr.assigned_at AS assignedAt,
                    cr.created_at AS createdAt,
                    cr.updated_at AS updatedAt
                FROM collection_requests cr
                WHERE cr.collector_id = :collectorId
                  AND cr.status IN ('assigned', 'accepted_collector', 'on_the_way', 'collected')
                ORDER BY
                    CASE WHEN cr.assigned_at IS NULL THEN 1 ELSE 0 END,
                    cr.assigned_at DESC,
                    cr.id DESC
            """, nativeQuery = true)
    Page<CollectorTaskView> findActiveTasksForCollector(@Param("collectorId") Integer collectorId, Pageable pageable);

    interface CollectorWorkHistoryView {
        Integer getId();

        String getRequestCode();

        String getStatus();

        String getAddress();

        String getWasteTypeCode();

        String getWasteTypeName();

        Integer getEnterpriseId();

        String getEnterpriseName();

        java.time.LocalDateTime getStartedAt();

        java.time.LocalDateTime getCollectedAt();

        java.time.LocalDateTime getCompletedAt();

        java.time.LocalDateTime getUpdatedAt();
    }

    interface CollectorMonthlyCompletedCountView {
        Integer getYear();

        Integer getMonth();

        Long getTotal();
    }

    @Query(value = """
                SELECT
                    cr.id AS id,
                    cr.request_code AS requestCode,
                    cr.status AS status,
                    wr.address AS address,
                    'RECYCLABLE' AS wasteTypeCode,
                    'Recyclable Waste' AS wasteTypeName,
                    e.id AS enterpriseId,
                    e.name AS enterpriseName,
                    cr.started_at AS startedAt,
                    cr.collected_at AS collectedAt,
                    cr.completed_at AS completedAt,
                    cr.updated_at AS updatedAt
                FROM collection_requests cr
                JOIN waste_reports wr ON cr.report_id = wr.id
                JOIN enterprise e ON cr.enterprise_id = e.id
                WHERE cr.collector_id = :collectorId
                  AND cr.status IN ('on_the_way', 'collected', 'completed', 'rejected')
                ORDER BY
                    COALESCE(cr.completed_at, cr.collected_at, cr.started_at, cr.updated_at) DESC,
                    cr.id DESC
            """, nativeQuery = true)
    Page<CollectorWorkHistoryView> findWorkHistoryForCollector(@Param("collectorId") Integer collectorId, Pageable pageable);

    @Query(value = """
                SELECT
                    cr.id AS id,
                    cr.request_code AS requestCode,
                    cr.status AS status,
                    wr.address AS address,
                    'RECYCLABLE' AS wasteTypeCode,
                    'Recyclable Waste' AS wasteTypeName,
                    e.id AS enterpriseId,
                    e.name AS enterpriseName,
                    cr.started_at AS startedAt,
                    cr.collected_at AS collectedAt,
                    cr.completed_at AS completedAt,
                    cr.updated_at AS updatedAt
                FROM collection_requests cr
                JOIN waste_reports wr ON cr.report_id = wr.id
                JOIN enterprise e ON cr.enterprise_id = e.id
                WHERE cr.collector_id = :collectorId
                  AND cr.status = :status
                ORDER BY
                    COALESCE(cr.completed_at, cr.collected_at, cr.started_at, cr.updated_at) DESC,
                    cr.id DESC
            """, nativeQuery = true)
    Page<CollectorWorkHistoryView> findWorkHistoryForCollectorByStatus(
            @Param("collectorId") Integer collectorId,
            @Param("status") String status,
            Pageable pageable);

    long countByCollector_IdAndStatus(Integer collectorId, CollectionRequestStatus status);

    @Query(value = """
                SELECT
                    YEAR(COALESCE(cr.completed_at, cr.collected_at)) AS year,
                    MONTH(COALESCE(cr.completed_at, cr.collected_at)) AS month,
                    COUNT(*) AS total
                FROM collection_requests cr
                WHERE cr.collector_id = :collectorId
                  AND cr.status IN ('collected', 'completed')
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND YEAR(COALESCE(cr.completed_at, cr.collected_at)) = :year
                GROUP BY YEAR(COALESCE(cr.completed_at, cr.collected_at)), MONTH(COALESCE(cr.completed_at, cr.collected_at))
                ORDER BY year DESC, month DESC
            """, nativeQuery = true)
    List<CollectorMonthlyCompletedCountView> countCompletedByMonth(
            @Param("collectorId") Integer collectorId,
            @Param("year") Integer year);

    Optional<CollectionRequest> findByIdAndCollector_Id(Integer id, Integer collectorId);

    /**
     * Cập nhật trạng thái theo điều kiện (atomic) - dùng khi muốn update bằng query
     * thay vì load entity.
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
            @Param("currentStatus") CollectionRequestStatus currentStatus,
            @Param("newStatus") CollectionRequestStatus newStatus,
            @Param("time") LocalDateTime time);

    @Modifying
    @Query("""
                UPDATE CollectionRequest cr
                SET cr.status = 'accepted_collector',
                    cr.acceptedAt = :time,
                    cr.updatedAt = :time
                WHERE cr.id = :id
                  AND cr.collector.id = :collectorId
                  AND cr.status = 'assigned'
            """)
    int acceptTask(
            @Param("id") Integer id,
            @Param("collectorId") Integer collectorId,
            @Param("time") LocalDateTime time);

    /**
     * Từ chối nhiệm vụ theo hướng atomic:
     * - Chỉ khi request đang assigned và thuộc collector hiện tại
     * - Set status = accepted_enterprise, lưu lý do, và unassign collector để
     * enterprise gán lại
     */
    @Modifying
    @Query("update CollectionRequest cr " +
            "set cr.status = 'accepted_enterprise'," +
            "cr.rejectionReason =:reason," +
            "cr.collector = null," +
            "cr.acceptedAt = null," +
            " cr.updatedAt = CURRENT_TIMESTAMP\n" +
            "        WHERE cr.id = :id\n" +
            "          AND cr.collector.id = :collectorId\n" +
            "          AND cr.status = 'assigned'")
    int rejectTask(
            @Param("id") Integer id,
            @Param("collectorId") Integer collectorId,
            @Param("reason") String reason);

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
            @Param("time") LocalDateTime time);

    /**
     * Xác nhận hoàn tất thu gom:
     * - Chỉ khi request đang collected và thuộc collector hiện tại
     * - Set status = completed, set completedAt
     */
    @Modifying
    @Query("""
                UPDATE CollectionRequest cr
                SET cr.status = 'completed',
                    cr.completedAt = :time,
                    cr.updatedAt = :time
                WHERE cr.id = :id
                  AND cr.collector.id = :collectorId
                  AND cr.status = 'collected'
            """)
    int confirmCompleted(
            @Param("id") Integer id,
            @Param("collectorId") Integer collectorId,
            @Param("time") LocalDateTime time);
}

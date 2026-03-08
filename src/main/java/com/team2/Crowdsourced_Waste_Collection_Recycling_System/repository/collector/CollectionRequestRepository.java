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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, Integer> {

    interface CollectorTaskView {
        Integer getId();

        String getRequestCode();

        String getStatus();

        String getAddress();

        java.time.LocalDateTime getAssignedAt();

        java.time.LocalDateTime getCreatedAt();

        java.time.LocalDateTime getUpdatedAt();
    }

    interface CollectorTaskStatusCountView {
        String getStatus();

        Long getTotal();
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
                  AND status IN ('accepted_enterprise', 'reassign')
            """, nativeQuery = true)
    int assignCollector(
            @Param("requestId") Integer requestId,
            @Param("collectorId") Integer collectorId,
            @Param("enterpriseId") Integer enterpriseId);

    @Query("SELECT cr FROM CollectionRequest cr WHERE cr.status = com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus.ASSIGNED AND cr.assignedAt < :threshold")
    List<CollectionRequest> findExpiredAssignedTasks(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT cr FROM CollectionRequest cr WHERE cr.status IN (com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus.ASSIGNED, com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus.ACCEPTED_COLLECTOR, com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus.ON_THE_WAY, com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus.COLLECTED) AND cr.assignedAt < :threshold")
    List<CollectionRequest> findSlaViolatedTasks(@Param("threshold") LocalDateTime threshold);

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
                  AND status IN ('accepted_enterprise', 'reassign')
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
                    wr.address AS address,
                    cr.assigned_at AS assignedAt,
                    cr.created_at AS createdAt,
                    cr.updated_at AS updatedAt
                FROM collection_requests cr
                LEFT JOIN waste_reports wr ON cr.report_id = wr.id
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
                    wr.address AS address,
                    cr.assigned_at AS assignedAt,
                    cr.created_at AS createdAt,
                    cr.updated_at AS updatedAt
                FROM collection_requests cr
                LEFT JOIN waste_reports wr ON cr.report_id = wr.id
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
                    wr.address AS address,
                    cr.assigned_at AS assignedAt,
                    cr.created_at AS createdAt,
                    cr.updated_at AS updatedAt
                FROM collection_requests cr
                LEFT JOIN waste_reports wr ON cr.report_id = wr.id
                WHERE cr.collector_id = :collectorId
                  AND cr.status IN ('assigned', 'accepted_collector', 'on_the_way', 'collected')
                ORDER BY
                    CASE WHEN cr.assigned_at IS NULL THEN 1 ELSE 0 END,
                    cr.assigned_at DESC,
                    cr.id DESC
            """, nativeQuery = true)
    Page<CollectorTaskView> findActiveTasksForCollector(@Param("collectorId") Integer collectorId, Pageable pageable);

    @Query(value = """
                SELECT
                    cr.status AS status,
                    COUNT(*) AS total
                FROM collection_requests cr
                WHERE cr.collector_id = :collectorId
                GROUP BY cr.status
            """, nativeQuery = true)
    List<CollectorTaskStatusCountView> countTasksByStatusForCollector(@Param("collectorId") Integer collectorId);

    @Query("SELECT req.status, COUNT(req) FROM CollectionRequest req WHERE req.enterprise.id = :enterpriseId GROUP BY req.status")
    List<Object[]> countStatusByEnterpriseId(@Param("enterpriseId") Integer enterpriseId);

    @Query("""
        SELECT req.collector.id, req.collector.fullName, COUNT(req), SUM(COALESCE(req.actualWeightKg, 0))
        FROM CollectionRequest req
        WHERE req.enterprise.id = :enterpriseId AND req.status = com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus.COMPLETED
        GROUP BY req.collector.id, req.collector.fullName
    """)
    List<Object[]> getCollectorPerformanceForEnterprise(@Param("enterpriseId") Integer enterpriseId);

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

    @Query("""
        SELECT c.id, c.fullName, COUNT(req), COALESCE(SUM(req.actualWeightKg), 0)
        FROM Collector c
        LEFT JOIN CollectionRequest req ON req.collector.id = c.id AND req.status = com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus.COMPLETED
        GROUP BY c.id, c.fullName
        ORDER BY COALESCE(SUM(req.actualWeightKg), 0) DESC
    """)
    List<Object[]> getGlobalCollectorPerformance();

    @Query(value = """
                SELECT
                    COALESCE(SUM(cr.actual_weight_kg), 0)
                FROM collection_requests cr
                WHERE cr.status = 'completed'
                  AND cr.actual_weight_kg IS NOT NULL
            """, nativeQuery = true)
    BigDecimal sumTotalActualWeight();

    long countByStatus(CollectionRequestStatus status);

    interface CollectorMonthlyCompletedCountView {
        Integer getYear();

        Integer getMonth();

        Long getTotal();
    }

    interface AdminMonthlyCollectedWeightView {
        Integer getYear();

        Integer getMonth();

        BigDecimal getTotalWeightKg();
    }

    interface AdminDailyCollectedWeightView {
        Integer getYear();

        Integer getMonth();

        Integer getDay();

        BigDecimal getTotalWeightKg();
    }

    interface CollectorMonthlyWasteVolumeView {
        Integer getYearValue();

        Integer getMonthValue();

        BigDecimal getTotalWeightKg();

        Long getTotalRequests();
    }

    interface CollectorQuarterlyWasteVolumeView {
        Integer getYearValue();

        Integer getQuarterValue();

        BigDecimal getTotalWeightKg();

        Long getTotalRequests();
    }

    interface EnterpriseMonthlyWasteVolumeView {
        Integer getYearValue();

        Integer getMonthValue();

        BigDecimal getTotalWeightKg();

        Long getTotalRequests();
    }

    interface EnterpriseQuarterlyWasteVolumeView {
        Integer getYearValue();

        Integer getQuarterValue();

        BigDecimal getTotalWeightKg();

        Long getTotalRequests();
    }

    interface EnterpriseCitizenPointSummaryView {
        Integer getCitizenId();

        String getFullName();

        String getPhone();

        Long getTotalPoints();

        BigDecimal getTotalWeightKg();

        Long getTotalCollections();
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

    @Query(value = """
                SELECT
                    YEAR(COALESCE(cr.completed_at, cr.collected_at)) AS year,
                    MONTH(COALESCE(cr.completed_at, cr.collected_at)) AS month,
                    COALESCE(SUM(cr.actual_weight_kg), 0) AS totalWeightKg
                FROM collection_requests cr
                WHERE cr.status = 'completed'
                  AND cr.actual_weight_kg IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND YEAR(COALESCE(cr.completed_at, cr.collected_at)) = :year
                GROUP BY YEAR(COALESCE(cr.completed_at, cr.collected_at)), MONTH(COALESCE(cr.completed_at, cr.collected_at))
                ORDER BY year ASC, month ASC
            """, nativeQuery = true)
    List<AdminMonthlyCollectedWeightView> sumActualWeightByMonthForYear(@Param("year") Integer year);

    @Query(value = """
                SELECT
                    COALESCE(SUM(cr.actual_weight_kg), 0)
                FROM collection_requests cr
                WHERE cr.status = 'completed'
                  AND cr.actual_weight_kg IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND YEAR(COALESCE(cr.completed_at, cr.collected_at)) = :year
            """, nativeQuery = true)
    BigDecimal sumActualWeightForYear(@Param("year") Integer year);

    @Query(value = """
                SELECT
                    YEAR(COALESCE(cr.completed_at, cr.collected_at)) AS year,
                    MONTH(COALESCE(cr.completed_at, cr.collected_at)) AS month,
                    DAY(COALESCE(cr.completed_at, cr.collected_at)) AS day,
                    COALESCE(SUM(cr.actual_weight_kg), 0) AS totalWeightKg
                FROM collection_requests cr
                WHERE cr.status = 'completed'
                  AND cr.actual_weight_kg IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND YEAR(COALESCE(cr.completed_at, cr.collected_at)) = :year
                  AND MONTH(COALESCE(cr.completed_at, cr.collected_at)) = :month
                GROUP BY YEAR(COALESCE(cr.completed_at, cr.collected_at)),
                         MONTH(COALESCE(cr.completed_at, cr.collected_at)),
                         DAY(COALESCE(cr.completed_at, cr.collected_at))
                ORDER BY day ASC
            """, nativeQuery = true)
    List<AdminDailyCollectedWeightView> sumActualWeightByDayForMonth(
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query(value = """
                SELECT
                    COALESCE(SUM(cr.actual_weight_kg), 0)
                FROM collection_requests cr
                WHERE cr.status = 'completed'
                  AND cr.actual_weight_kg IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND YEAR(COALESCE(cr.completed_at, cr.collected_at)) = :year
                  AND MONTH(COALESCE(cr.completed_at, cr.collected_at)) = :month
            """, nativeQuery = true)
    BigDecimal sumActualWeightForMonth(
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query(value = """
                SELECT
                    YEAR(COALESCE(cr.completed_at, cr.collected_at)) AS yearValue,
                    MONTH(COALESCE(cr.completed_at, cr.collected_at)) AS monthValue,
                    SUM(COALESCE(cr.actual_weight_kg, 0)) AS totalWeightKg,
                    COUNT(*) AS totalRequests
                FROM collection_requests cr
                WHERE cr.collector_id = :collectorId
                  AND cr.status = 'completed'
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) >= :from
                  AND COALESCE(cr.completed_at, cr.collected_at) < :to
                GROUP BY YEAR(COALESCE(cr.completed_at, cr.collected_at)), MONTH(COALESCE(cr.completed_at, cr.collected_at))
                ORDER BY yearValue ASC, monthValue ASC
            """, nativeQuery = true)
    List<CollectorMonthlyWasteVolumeView> sumCompletedWeightByMonthForCollector(
            @Param("collectorId") Integer collectorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
                SELECT
                    YEAR(COALESCE(cr.completed_at, cr.collected_at)) AS yearValue,
                    FLOOR((MONTH(COALESCE(cr.completed_at, cr.collected_at)) + 2) / 3) AS quarterValue,
                    SUM(COALESCE(cr.actual_weight_kg, 0)) AS totalWeightKg,
                    COUNT(*) AS totalRequests
                FROM collection_requests cr
                WHERE cr.collector_id = :collectorId
                  AND cr.status = 'completed'
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) >= :from
                  AND COALESCE(cr.completed_at, cr.collected_at) < :to
                GROUP BY YEAR(COALESCE(cr.completed_at, cr.collected_at)), FLOOR((MONTH(COALESCE(cr.completed_at, cr.collected_at)) + 2) / 3)
                ORDER BY yearValue ASC, quarterValue ASC
            """, nativeQuery = true)
    List<CollectorQuarterlyWasteVolumeView> sumCompletedWeightByQuarterForCollector(
            @Param("collectorId") Integer collectorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
                SELECT
                    YEAR(COALESCE(cr.completed_at, cr.collected_at)) AS yearValue,
                    MONTH(COALESCE(cr.completed_at, cr.collected_at)) AS monthValue,
                    SUM(COALESCE(cr.actual_weight_kg, 0)) AS totalWeightKg,
                    COUNT(*) AS totalRequests
                FROM collection_requests cr
                WHERE cr.enterprise_id = :enterpriseId
                  AND cr.status = 'completed'
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) >= :from
                  AND COALESCE(cr.completed_at, cr.collected_at) < :to
                GROUP BY YEAR(COALESCE(cr.completed_at, cr.collected_at)), MONTH(COALESCE(cr.completed_at, cr.collected_at))
                ORDER BY yearValue ASC, monthValue ASC
            """, nativeQuery = true)
    List<EnterpriseMonthlyWasteVolumeView> sumCompletedWeightByMonthForEnterprise(
            @Param("enterpriseId") Integer enterpriseId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
                SELECT
                    YEAR(COALESCE(cr.completed_at, cr.collected_at)) AS yearValue,
                    FLOOR((MONTH(COALESCE(cr.completed_at, cr.collected_at)) + 2) / 3) AS quarterValue,
                    SUM(COALESCE(cr.actual_weight_kg, 0)) AS totalWeightKg,
                    COUNT(*) AS totalRequests
                FROM collection_requests cr
                WHERE cr.enterprise_id = :enterpriseId
                  AND cr.status = 'completed'
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) >= :from
                  AND COALESCE(cr.completed_at, cr.collected_at) < :to
                GROUP BY YEAR(COALESCE(cr.completed_at, cr.collected_at)), FLOOR((MONTH(COALESCE(cr.completed_at, cr.collected_at)) + 2) / 3)
                ORDER BY yearValue ASC, quarterValue ASC
            """, nativeQuery = true)
    List<EnterpriseQuarterlyWasteVolumeView> sumCompletedWeightByQuarterForEnterprise(
            @Param("enterpriseId") Integer enterpriseId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
                SELECT
                    c.id AS citizenId,
                    c.full_name AS fullName,
                    c.phone AS phone,
                    SUM(COALESCE(pt.points, 0)) AS totalPoints,
                    SUM(COALESCE(cr.actual_weight_kg, 0)) AS totalWeightKg,
                    COUNT(DISTINCT cr.id) AS totalCollections
                FROM collection_requests cr
                JOIN waste_reports wr ON cr.report_id = wr.id
                JOIN citizens c ON wr.citizen_id = c.id
                LEFT JOIN point_transactions pt
                       ON pt.collection_request_id = cr.id
                      AND pt.transaction_type = 'EARN'
                WHERE cr.enterprise_id = :enterpriseId
                  AND cr.status = 'completed'
                  AND COALESCE(cr.completed_at, cr.collected_at) IS NOT NULL
                  AND COALESCE(cr.completed_at, cr.collected_at) >= :from
                  AND COALESCE(cr.completed_at, cr.collected_at) < :to
                GROUP BY c.id, c.full_name, c.phone
                ORDER BY totalPoints DESC, totalWeightKg DESC, citizenId ASC
            """, nativeQuery = true)
    List<EnterpriseCitizenPointSummaryView> summarizeCitizenPointsForEnterprise(
            @Param("enterpriseId") Integer enterpriseId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    Optional<CollectionRequest> findByIdAndCollector_Id(Integer id, Integer collectorId);

    @Query("""
                SELECT cr
                FROM CollectionRequest cr
                JOIN FETCH cr.report r
                WHERE cr.enterprise.id = :enterpriseId
                  AND cr.status = com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus.REASSIGN
                ORDER BY cr.updatedAt DESC, cr.id DESC
            """)
    List<CollectionRequest> findCollectorRejectedRequests(@Param("enterpriseId") Integer enterpriseId);

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
     * - Set status = reassign, lưu lý do, và unassign collector để enterprise gán lại
     */
    @Modifying
    @Query("update CollectionRequest cr " +
            "set cr.status = 'reassign'," +
            "cr.rejectionReason =:reason," +
            "cr.collector = null," +
            "cr.assignedAt = null," +
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

    @Modifying
    @Query("""
                UPDATE CollectionRequest cr
                SET cr.status = 'completed',
                    cr.completedAt = :time,
                    cr.actualWeightKg = :actualWeightKg,
                    cr.updatedAt = :time
                WHERE cr.id = :id
                  AND cr.collector.id = :collectorId
                  AND cr.status = 'collected'
            """)
    int confirmCompletedWithWeight(
            @Param("id") Integer id,
            @Param("collectorId") Integer collectorId,
            @Param("actualWeightKg") BigDecimal actualWeightKg,
            @Param("time") LocalDateTime time);
}

package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorPerformanceStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorTaskResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorWorkHistoryItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CollectorService {
    /**
     * Lấy danh sách task của collector (phân trang).
     * - Nếu all=true: lấy tất cả.
     * - Nếu status != null: lấy theo status.
     * - Ngược lại: lấy active tasks (ASSIGNED, ACCEPTED_COLLECTOR, ON_THE_WAY).
     */
    Page<CollectorTaskResponse> getTasks(Integer collectorId, String status, boolean all, Pageable pageable);

    /**
     * Lấy lịch sử công việc của collector (phân trang).
     */
    Page<CollectorWorkHistoryItemResponse> getWorkHistory(Integer collectorId, String status, Pageable pageable);

    /**
     * Lấy thống kê hiệu suất của collector.
     */
    CollectorPerformanceStatsResponse getStats(Integer collectorId, Integer year);

    /**
     * Collector chấp nhận nhiệm vụ: assigned -> accepted_collector.
     */
    void acceptTask(Integer requestId, Integer collectorId);

    /**
     * Collector bắt đầu di chuyển: accepted_collector -> on_the_way.
     */
    void startTask(Integer requestId, Integer collectorId);

    /**
     * Collector từ chối nhiệm vụ (chỉ khi assigned):
     * - status -> accepted_enterprise
     * - unassign collector để enterprise phân công lại
     */
    void rejectTask(Integer requestId, Integer collectorId, String reason);

    /**
     * Cập nhật trạng thái nhiệm vụ.
     */
    void updateStatus(Integer requestId, Integer collectorId, String status);

    /**
     * Collector hoàn thành nhiệm vụ (chỉ cập nhật status):
     * - on_the_way -> collected
     * Lưu ý: nếu cần upload ảnh + tạo report, dùng CollectorReportService.
     */
    void completeTask(Integer requestId, Integer collectorId);
}

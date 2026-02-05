package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

public interface CollectorService {
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
     * Collector hoàn thành nhiệm vụ (chỉ cập nhật status):
     * - on_the_way -> collected
     * Lưu ý: nếu cần upload ảnh + tạo report, dùng CollectorReportService.
     */
    void completeTask(Integer requestId, Integer collectorId);
}

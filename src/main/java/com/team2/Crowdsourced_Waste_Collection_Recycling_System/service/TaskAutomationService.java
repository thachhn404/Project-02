package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

public interface TaskAutomationService {
    /**
     * Kiểm tra các task ASSIGNED đã quá 4 giờ mà chưa được xử lý -> Tự động điều phối lại.
     */
    void checkAssignedTasksTimeout();

    /**
     * Kiểm tra các task vi phạm SLA (quá 72 giờ chưa hoàn thành) -> Ghi nhận vi phạm, có thể khóa tài khoản Collector.
     */
    void checkSlaViolations();
}

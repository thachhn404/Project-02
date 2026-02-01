package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

public interface CollectorService {
    /**
     * Collector chấp nhận nhiệm vụ (ghi audit log, status vẫn là ASSIGNED).
     */
    void acceptTask(Integer requestId, Integer collectorId);

    /**
     * Collector bắt đầu nhiệm vụ (chuyển sang ON_THE_WAY).
     */
    void startTask(Integer requestId, Integer collectorId);

    /**
     * Collector từ chối nhiệm vụ (chuyển về ACCEPTED để enterprise gán lại).
     */
    void rejectTask(Integer requestId, Integer collectorId, String reason);

    /**
     * Collector hoàn thành nhiệm vụ (chuyển sang COLLECTED).
     */
    void completeTask(Integer requestId, Integer collectorId);
}

package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorMonthlyCompletedCountResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorMonthlyWasteVolumeResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorPerformanceStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorQuarterlyWasteVolumeResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorTaskResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorTaskStatusCountResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorWasteVolumeStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorWorkHistoryItemResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseAssignmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;

@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {

    // Khai báo các Repository cần thiết để làm việc với database
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;
    private final CollectorRepository collectorRepository;
    private final WasteReportRepository wasteReportRepository;
    private final EnterpriseAssignmentService enterpriseAssignmentService;
    private final CollectorReportItemRepository collectorReportItemRepository;

    /**
     * Lấy danh sách nhiệm vụ (tasks) của Collector.
     * Có thể lọc theo trạng thái hoặc lấy tất cả.
     */
    @Override
    public List<CollectorTaskResponse> getTasks(Integer collectorId, String status, boolean all) {
        // Sử dụng unpaged để lấy tất cả dữ liệu mà không phân trang
        Pageable unpaged = Pageable.unpaged();
        Page<CollectionRequestRepository.CollectorTaskView> tasksPage;

        // Kiểm tra điều kiện để gọi query tương ứng
        if (all) {
            // Lấy tất cả task của collector này
            tasksPage = collectionRequestRepository.findTasksForCollector(collectorId, unpaged);
        } else if (status != null && !status.trim().isEmpty()) {
            // Lấy task theo trạng thái cụ thể (ví dụ: "ASSIGNED")
            tasksPage = collectionRequestRepository.findTasksForCollectorByStatus(collectorId, status, unpaged);
        } else {
            // Mặc định: Lấy các task đang hoạt động (active)
            tasksPage = collectionRequestRepository.findActiveTasksForCollector(collectorId, unpaged);
        }

        // Chuyển đổi dữ liệu từ Entity sang DTO để trả về cho Client
        List<CollectionRequestRepository.CollectorTaskView> taskEntities = tasksPage.getContent();
        List<CollectorTaskResponse> responseList = new ArrayList<>();

        for (CollectionRequestRepository.CollectorTaskView task : taskEntities) {
            // Tạo đối tượng response thủ công
            CollectorTaskResponse dto = new CollectorTaskResponse();
            dto.setId(task.getId());
            dto.setRequestCode(task.getRequestCode());
            dto.setStatus(task.getStatus());
            dto.setAddress(task.getAddress());
            dto.setAssignedAt(task.getAssignedAt());
            dto.setCreatedAt(task.getCreatedAt());
            dto.setUpdatedAt(task.getUpdatedAt());

            // Thêm vào danh sách kết quả
            responseList.add(dto);
        }

        return responseList;
    }

    /**
     * Đếm số lượng task theo từng trạng thái.
     * Ví dụ: ASSIGNED: 5, COMPLETED: 10
     */
    @Override
    public List<CollectorTaskStatusCountResponse> getTaskStatusCounts(Integer collectorId) {
        // Lấy dữ liệu thô từ database
        List<CollectionRequestRepository.CollectorTaskStatusCountView> rows =
                collectionRequestRepository.countTasksByStatusForCollector(collectorId);

        // Chuyển list kết quả thành Map để dễ tra cứu (Key: status, Value: count)
        Map<String, Long> countMap = new HashMap<>();
        for (CollectionRequestRepository.CollectorTaskStatusCountView row : rows) {
            if (row != null && row.getStatus() != null) {
                Long total = row.getTotal();
                if (total == null) {
                    total = 0L;
                }
                countMap.put(row.getStatus(), total);
            }
        }

        // Tạo danh sách kết quả dựa trên tất cả các trạng thái có thể có
        List<CollectorTaskStatusCountResponse> resultList = new ArrayList<>();
        CollectionRequestStatus[] allStatuses = CollectionRequestStatus.values();

        for (CollectionRequestStatus statusEnum : allStatuses) {
            String statusKey = statusEnum.name().toLowerCase();
            // Lấy số lượng từ map, nếu không có thì mặc định là 0
            Long count = countMap.getOrDefault(statusKey, 0L);

            CollectorTaskStatusCountResponse response = new CollectorTaskStatusCountResponse();
            response.setStatus(statusKey);
            response.setTotal(count);

            resultList.add(response);
        }

        return resultList;
    }

    /**
     * Lấy lịch sử làm việc của Collector.
     */
    @Override
    public List<CollectorWorkHistoryItemResponse> getWorkHistory(Integer collectorId, String status) {
        Pageable unpaged = Pageable.unpaged();
        Page<CollectionRequestRepository.CollectorWorkHistoryView> historyPage;

        // Kiểm tra xem có lọc theo status hay không
        if (status != null && !status.trim().isEmpty()) {
            historyPage = collectionRequestRepository.findWorkHistoryForCollectorByStatus(collectorId, status, unpaged);
        } else {
            historyPage = collectionRequestRepository.findWorkHistoryForCollector(collectorId, unpaged);
        }

        // Chuyển đổi dữ liệu sang DTO
        List<CollectionRequestRepository.CollectorWorkHistoryView> historyEntities = historyPage.getContent();
        List<CollectorWorkHistoryItemResponse> responseList = new ArrayList<>();

        for (CollectionRequestRepository.CollectorWorkHistoryView item : historyEntities) {
            CollectorWorkHistoryItemResponse dto = new CollectorWorkHistoryItemResponse();
            dto.setCollectionRequestId(item.getId());
            dto.setRequestCode(item.getRequestCode());
            dto.setStatus(item.getStatus());
            dto.setAddress(item.getAddress());
            dto.setWasteTypeCode(item.getWasteTypeCode());
            dto.setWasteTypeName(item.getWasteTypeName());
            dto.setEnterpriseId(item.getEnterpriseId());
            dto.setEnterpriseName(item.getEnterpriseName());
            dto.setStartedAt(item.getStartedAt());
            dto.setCollectedAt(item.getCollectedAt());
            dto.setCompletedAt(item.getCompletedAt());
            dto.setUpdatedAt(item.getUpdatedAt());

            responseList.add(dto);
        }

        return responseList;
    }

    /**
     * Lấy thống kê hiệu suất làm việc theo năm.
     */
    @Override
    public CollectorPerformanceStatsResponse getStats(Integer collectorId, Integer year) {
        // Nếu không truyền năm, lấy năm hiện tại
        int selectedYear;
        if (year != null) {
            selectedYear = year;
        } else {
            selectedYear = LocalDate.now().getYear();
        }

        // Tính tổng số task đã hoàn thành (COMPLETED hoặc COLLECTED)
        long countCompleted = collectionRequestRepository.countByCollector_IdAndStatus(collectorId, CollectionRequestStatus.COMPLETED);
        long countCollected = collectionRequestRepository.countByCollector_IdAndStatus(collectorId, CollectionRequestStatus.COLLECTED);
        long totalCompleted = countCompleted + countCollected;

        // Lấy thống kê theo từng tháng từ DB
        List<CollectionRequestRepository.CollectorMonthlyCompletedCountView> monthlyData =
                collectionRequestRepository.countCompletedByMonth(collectorId, selectedYear);

        // Chuyển đổi sang list DTO
        List<CollectorMonthlyCompletedCountResponse> completedByMonthList = new ArrayList<>();
        for (CollectionRequestRepository.CollectorMonthlyCompletedCountView row : monthlyData) {
            CollectorMonthlyCompletedCountResponse dto = new CollectorMonthlyCompletedCountResponse();
            dto.setYear(row.getYear());
            dto.setMonth(row.getMonth());
            dto.setTotal(row.getTotal());
            completedByMonthList.add(dto);
        }

        // Đóng gói kết quả
        CollectorPerformanceStatsResponse response = new CollectorPerformanceStatsResponse();
        response.setTotalCompleted(totalCompleted);
        response.setYear(selectedYear);
        response.setCompletedByMonth(completedByMonthList);

        return response;
    }

    /**
     * Thống kê khối lượng rác thu gom được.
     */
    @Override
    public CollectorWasteVolumeStatsResponse getWasteVolumeStats(Integer collectorId, Integer year) {
        // Xử lý năm
        int y;
        if (year != null) {
            y = year;
        } else {
            y = LocalDate.now().getYear();
        }

        // Validate năm hợp lệ
        if (y < 1970 || y > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Năm không hợp lệ (phải từ 1970 đến 2100)");
        }

        // Xác định khoảng thời gian đầu năm đến cuối năm
        LocalDateTime fromDate = LocalDate.of(y, 1, 1).atStartOfDay();
        LocalDateTime toDate = fromDate.plusYears(1);

        // Lấy dữ liệu từ DB
        List<CollectionRequestRepository.CollectorMonthlyWasteVolumeView> monthlyViews =
                collectionRequestRepository.sumCompletedWeightByMonthForCollector(collectorId, fromDate, toDate);
        List<CollectionRequestRepository.CollectorQuarterlyWasteVolumeView> quarterlyViews =
                collectionRequestRepository.sumCompletedWeightByQuarterForCollector(collectorId, fromDate, toDate);

        // 1. Xử lý thống kê theo Tháng
        List<CollectorMonthlyWasteVolumeResponse> monthlyResponseList = new ArrayList<>();
        BigDecimal totalWeightAllMonths = BigDecimal.ZERO;
        long totalRequestsAllMonths = 0L;

        for (CollectionRequestRepository.CollectorMonthlyWasteVolumeView view : monthlyViews) {
            CollectorMonthlyWasteVolumeResponse dto = new CollectorMonthlyWasteVolumeResponse();
            dto.setYear(view.getYearValue());
            dto.setMonth(view.getMonthValue());
            
            // Xử lý null cho weight
            BigDecimal weight = view.getTotalWeightKg();
            if (weight == null) {
                weight = BigDecimal.ZERO;
            }
            dto.setTotalWeightKg(weight);

            // Xử lý null cho total requests
            Long requests = view.getTotalRequests();
            if (requests == null) {
                requests = 0L;
            }
            dto.setTotalRequests(requests);

            monthlyResponseList.add(dto);

            // Cộng dồn tổng
            totalWeightAllMonths = totalWeightAllMonths.add(weight);
            totalRequestsAllMonths = totalRequestsAllMonths + requests;
        }

        // 2. Xử lý thống kê theo Quý
        List<CollectorQuarterlyWasteVolumeResponse> quarterlyResponseList = new ArrayList<>();
        for (CollectionRequestRepository.CollectorQuarterlyWasteVolumeView view : quarterlyViews) {
            CollectorQuarterlyWasteVolumeResponse dto = new CollectorQuarterlyWasteVolumeResponse();
            dto.setYear(view.getYearValue());
            dto.setQuarter(view.getQuarterValue());
            
            BigDecimal weight = view.getTotalWeightKg();
            if (weight == null) {
                weight = BigDecimal.ZERO;
            }
            dto.setTotalWeightKg(weight);

            Long requests = view.getTotalRequests();
            if (requests == null) {
                requests = 0L;
            }
            dto.setTotalRequests(requests);

            quarterlyResponseList.add(dto);
        }

        // Tạo object kết quả cuối cùng
        CollectorWasteVolumeStatsResponse response = new CollectorWasteVolumeStatsResponse();
        response.setYear(y);
        response.setTotalWeightKg(totalWeightAllMonths);
        response.setTotalRequests(totalRequestsAllMonths);
        response.setByMonth(monthlyResponseList);
        response.setByQuarter(quarterlyResponseList);

        return response;
    }

    /**
     * Thống kê khối lượng rác theo từng loại rác.
     */
    @Override
    public Map<String, BigDecimal> getWasteTypeStats(Integer collectorId) {
        List<Object[]> rows = collectorReportItemRepository.sumWeightByWasteTypeForCollector(collectorId);
        Map<String, BigDecimal> resultMap = new HashMap<>();

        for (Object[] row : rows) {
            String wasteType = (String) row[0];
            BigDecimal weight = (BigDecimal) row[1];

            if (weight == null) {
                weight = BigDecimal.ZERO;
            }
            
            resultMap.put(wasteType, weight);
        }
        return resultMap;
    }

    /**
     * Cập nhật trạng thái nhiệm vụ (chỉ hỗ trợ ON_THE_WAY).
     */
    @Override
    @Transactional
    public void updateStatus(Integer requestId, Integer collectorId, String statusStr) {
        if ("ON_THE_WAY".equalsIgnoreCase(statusStr)) {
            startTask(requestId, collectorId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ. Chỉ chấp nhận 'ON_THE_WAY'.");
        }
    }

    /**
     * Collector chấp nhận nhiệm vụ.
     * Chuyển trạng thái từ ASSIGNED -> ACCEPTED_COLLECTOR
     */
    @Override
    @Transactional
    public void acceptTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        
        // Cập nhật trong DB và trả về số dòng bị ảnh hưởng
        int updatedCount = collectionRequestRepository.acceptTask(requestId, collectorId, now);
        
        // Nếu không cập nhật được dòng nào (có thể do sai ID hoặc sai trạng thái cũ)
        if (updatedCount == 0) {
            handleUpdateError(requestId, collectorId, CollectionRequestStatus.ASSIGNED);
        }
        
        // Cập nhật trạng thái report liên quan (nếu có)
        updateReportStatus(requestId, WasteReportStatus.ACCEPTED_COLLECTOR, now);
        
        // Ghi log tracking
        saveTrackingLog(requestId, collectorId, "accepted", "Collector đã chấp nhận nhiệm vụ");
    }

    /**
     * Collector bắt đầu di chuyển.
     * Chuyển trạng thái từ ACCEPTED_COLLECTOR -> ON_THE_WAY
     */
    @Override
    @Transactional
    public void startTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        
        int updatedCount = collectionRequestRepository.updateStatusIfMatch(
                requestId, collectorId, CollectionRequestStatus.ACCEPTED_COLLECTOR, CollectionRequestStatus.ON_THE_WAY, now);
        
        if (updatedCount == 0) {
            handleUpdateError(requestId, collectorId, CollectionRequestStatus.ACCEPTED_COLLECTOR);
        }
        
        updateReportStatus(requestId, WasteReportStatus.ON_THE_WAY, now);
        saveTrackingLog(requestId, collectorId, "started", "Collector bắt đầu di chuyển");
    }

    /**
     * Collector từ chối nhiệm vụ.
     */
    @Override
    @Transactional
    public void rejectTask(Integer requestId, Integer collectorId, String reason) {
        // Kiểm tra lý do từ chối
        if (reason == null || reason.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bắt buộc phải có lý do từ chối");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Thực hiện từ chối trong DB
        int updatedCount = collectionRequestRepository.rejectTask(requestId, collectorId, reason);
        
        if (updatedCount == 0) {
            // Xử lý lỗi cụ thể cho trường hợp từ chối
            Optional<CollectionRequest> optRequest = collectionRequestRepository.findById(requestId);
            if (optRequest.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu thu gom");
            }
            
            CollectionRequest req = optRequest.get();
            // Kiểm tra quyền sở hữu
            if (req.getCollector() == null || !req.getCollector().getId().equals(collectorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Yêu cầu này không thuộc về bạn");
            }
            
            // Kiểm tra trạng thái
            if (req.getStatus() != CollectionRequestStatus.ASSIGNED) {
                String msg = "Trạng thái không hợp lệ. Mong đợi ASSIGNED nhưng thực tế là " + req.getStatus();
                if (req.getStatus() == CollectionRequestStatus.ON_THE_WAY) {
                    msg += ". Không thể từ chối khi đã đi.";
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
            }
            
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lỗi không xác định khi từ chối nhiệm vụ");
        }
        
        updateReportStatus(requestId, WasteReportStatus.REASSIGN, now);
        saveTrackingLog(requestId, collectorId, "rejected", "Collector từ chối: " + reason);
        autoAssignAnotherCollectorIfPossible(requestId, collectorId);
    }

    private void autoAssignAnotherCollectorIfPossible(Integer requestId, Integer rejectedCollectorId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId).orElse(null);
        if (request == null || request.getEnterprise() == null || request.getEnterprise().getId() == null) {
            return;
        }
        Integer enterpriseId = request.getEnterprise().getId();

        var candidates = collectorRepository.findAvailableCollectors(enterpriseId);
        Integer bestCollectorId = null;
        long bestActive = Long.MAX_VALUE;
        for (var c : candidates) {
            if (c == null || c.getId() == null) {
                continue;
            }
            if (rejectedCollectorId != null && c.getId().equals(rejectedCollectorId)) {
                continue;
            }
            long active = collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ASSIGNED)
                    + collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ACCEPTED_COLLECTOR)
                    + collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ON_THE_WAY);
            if (active < bestActive) {
                bestActive = active;
                bestCollectorId = c.getId();
            }
        }
        if (bestCollectorId == null) {
            return;
        }
        enterpriseAssignmentService.assignCollector(enterpriseId, requestId, bestCollectorId);
    }

    /**
     * Collector hoàn thành nhiệm vụ.
     * Chuyển trạng thái từ ON_THE_WAY -> COLLECTED
     */
    @Override
    @Transactional
    public void completeTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        
        int updatedCount = collectionRequestRepository.completeTask(requestId, collectorId, now);
        
        if (updatedCount == 0) {
            handleUpdateError(requestId, collectorId, CollectionRequestStatus.ON_THE_WAY);
        }
        
        updateReportStatus(requestId, WasteReportStatus.COLLECTED, now);
        saveTrackingLog(requestId, collectorId, "collected", "Collector đã hoàn thành thu gom");
    }

    /**
     * Cập nhật trạng thái làm việc của chính Collector (VD: Online, Offline).
     */
    @Override
    @Transactional
    public void updateAvailabilityStatus(Integer collectorId, String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không được để trống");
        }

        CollectorStatus newStatus;
        try {
            newStatus = CollectorStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
        }

        if (newStatus == CollectorStatus.SUSPEND) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không được phép tự chuyển sang trạng thái SUSPEND");
        }

        // Tìm Collector và cập nhật
        Optional<Collector> collectorOpt = collectorRepository.findById(collectorId);
        if (collectorOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Collector");
        }

        Collector collector = collectorOpt.get();
        collector.setStatus(newStatus);
        collectorRepository.save(collector);
    }

    // --- Các hàm hỗ trợ (Helper Methods) ---

    private void updateReportStatus(Integer requestId, WasteReportStatus newStatus, LocalDateTime now) {
        // Tìm request, nếu không thấy thì bỏ qua (dùng orElse(null))
        CollectionRequest request = collectionRequestRepository.findById(requestId).orElse(null);
        
        if (request != null && request.getReport() != null) {
            var report = request.getReport();
            report.setStatus(newStatus);
            report.setUpdatedAt(now);
            wasteReportRepository.save(report);
        }
    }

    private void saveTrackingLog(Integer requestId, Integer collectorId, String action, String note) {
        CollectionTracking tracking = new CollectionTracking();
        
        // Dùng getReferenceById để tránh query thừa, chỉ lấy proxy để gán khóa ngoại
        tracking.setCollectionRequest(collectionRequestRepository.getReferenceById(requestId));
        tracking.setCollector(collectorRepository.getReferenceById(collectorId));
        
        tracking.setAction(action);
        tracking.setNote(note);
        tracking.setCreatedAt(LocalDateTime.now());
        
        collectionTrackingRepository.save(tracking);
    }

    private void handleUpdateError(Integer requestId, Integer collectorId, CollectionRequestStatus expectedStatus) {
        // Lấy thông tin request để báo lỗi chi tiết
        Optional<CollectionRequest> optRequest = collectionRequestRepository.findById(requestId);
        
        if (optRequest.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Collection Request");
        }
        
        CollectionRequest request = optRequest.get();
        
        // Kiểm tra xem request có đúng của collector này không
        if (request.getCollector() == null || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request này không thuộc về bạn");
        }
        
        // Kiểm tra trạng thái hiện tại so với mong đợi
        if (request.getStatus() != expectedStatus) {
            String msg = "Trạng thái không hợp lệ. Mong đợi '" + expectedStatus + "' nhưng thực tế là '" + request.getStatus() + "'.";
            
            // Thêm thông báo cụ thể cho trường hợp phổ biến
            if (request.getStatus() == CollectionRequestStatus.ON_THE_WAY) {
                msg += " Không thể thay đổi khi đã bắt đầu di chuyển.";
            }
            
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }
        
        // Nếu mọi thứ có vẻ đúng mà vẫn không update được (ít khi xảy ra)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể cập nhật trạng thái Collection Request (Lỗi không xác định)");
    }
}

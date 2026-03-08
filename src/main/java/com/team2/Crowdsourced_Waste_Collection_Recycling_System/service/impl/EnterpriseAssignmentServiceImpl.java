package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EligibleCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.RequestPreviewResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseAssignmentService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class EnterpriseAssignmentServiceImpl implements EnterpriseAssignmentService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorRepository collectorRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;
    private final WasteReportRepository wasteReportRepository;
    private final EnterpriseRequestService enterpriseRequestService;

    @Override
    @Transactional
    public AssignCollectorResponse assignCollector(Integer enterpriseId, Integer requestId, Integer collectorId) {
        // Kiểm tra thông tin đầu vào
        requireEnterpriseId(enterpriseId);
        requireRequestId(requestId);
        requireCollectorId(collectorId);

        // Kiểm tra collector có thuộc enterprise và đang active không
        Collector collector = requireEnterpriseCollector(enterpriseId, collectorId);
        validateCollectorAssignable(collector);

        // Kiểm tra request
        CollectionRequest requestBeforeAssign = requireEnterpriseRequest(enterpriseId, requestId);
        
        // Nếu request đang ở trạng thái REASSIGN, không được gán lại cho người vừa từ chối
        if (requestBeforeAssign.getStatus() == CollectionRequestStatus.REASSIGN) {
            var tracking = collectionTrackingRepository
                    .findFirstByCollectionRequest_IdAndActionOrderByCreatedAtDesc(requestId, "rejected")
                    .orElse(null);
            
            if (tracking != null && tracking.getCollector() != null) {
                Integer lastRejectedCollectorId = tracking.getCollector().getId();
                if (lastRejectedCollectorId.equals(collectorId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Không thể gán lại cho collector vừa từ chối nhiệm vụ");
                }
            }
        }

        // Thực hiện cập nhật DB để gán collector
        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.assignCollector(requestId, collectorId, enterpriseId);
        
        // Nếu cập nhật thất bại (không có dòng nào thay đổi), ném lỗi
        if (updated == 0) {
            throw explainAssignFailure(enterpriseId, requestId);
        }

        // Lấy lại request sau khi cập nhật để xử lý tiếp
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        Integer collectionRequestId = request.getId();
        WasteReport report = request.getReport();
        
        // Cập nhật trạng thái báo cáo rác thành ASSIGNED
        if (report == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Collection Request thiếu report_id, không thể cập nhật WasteReport");
        }
        report.setStatus(WasteReportStatus.ASSIGNED);
        report.setUpdatedAt(now);
        wasteReportRepository.saveAndFlush(report);

        // Lưu lịch sử (tracking)
        CollectionTracking tracking = new CollectionTracking();
        tracking.setCollectionRequest(collectionRequestRepository.getReferenceById(collectionRequestId));
        tracking.setCollector(collector);
        tracking.setAction("assigned");
        tracking.setNote("Enterprise assigned collector");
        tracking.setCreatedAt(now);
        collectionTrackingRepository.save(tracking);

        // Trả về kết quả
        AssignCollectorResponse response = new AssignCollectorResponse();
        response.setCollectionRequestId(collectionRequestId);
        response.setCollectorId(collector.getId());
        response.setStatus("assigned");
        response.setAssignedAt(now);
        return response;
    }

    @Override
    @Transactional
    public AssignCollectorResponse assignCollectorByReportCode(Integer enterpriseId, String reportCode, Integer collectorId) {
        requireEnterpriseId(enterpriseId);
        requireReportCode(reportCode);
        requireCollectorId(collectorId);

        Collector collector = requireEnterpriseCollector(enterpriseId, collectorId);
        validateCollectorAssignable(collector);

        var report = wasteReportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waste Report không tồn tại"));
        var existing = collectionRequestRepository.findByReport_Id(report.getId());
        Integer reqId;
        if (existing.isEmpty()) {
            reqId = enterpriseRequestService.acceptWasteReport(enterpriseId, reportCode);
        } else {
            reqId = existing.get().getId();
        }
        return assignCollector(enterpriseId, reqId, collectorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibleCollectorResponse> findEligibleCollectors(Integer enterpriseId, Integer requestId, Double radiusKm) {
        // Kiểm tra thông tin đầu vào
        requireEnterpriseId(enterpriseId);
        requireRequestId(requestId);

        // Lấy thông tin request và kiểm tra trạng thái
        CollectionRequest request = requireEnterpriseRequest(enterpriseId, requestId);
        if (request.getStatus() != CollectionRequestStatus.ACCEPTED_ENTERPRISE
                && request.getStatus() != CollectionRequestStatus.REASSIGN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ hỗ trợ tìm collector khi request ở trạng thái ACCEPTED_ENTERPRISE hoặc REASSIGN");
        }

        // Nếu là REASSIGN, tìm collector vừa từ chối để loại bỏ
        Integer lastRejectedCollectorId = null;
        if (request.getStatus() == CollectionRequestStatus.REASSIGN) {
            var tracking = collectionTrackingRepository
                    .findFirstByCollectionRequest_IdAndActionOrderByCreatedAtDesc(requestId, "rejected")
                    .orElse(null);
            
            if (tracking != null && tracking.getCollector() != null) {
                lastRejectedCollectorId = tracking.getCollector().getId();
            }
        }

        // Lấy danh sách tất cả collector của doanh nghiệp
        List<Collector> allCollectors = collectorRepository.findByEnterprise_IdOrderByCreatedAtDesc(enterpriseId);
        List<EligibleCollectorResponse> result = new java.util.ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Duyệt qua từng collector để kiểm tra điều kiện
        for (Collector collector : allCollectors) {
            // 1. Bỏ qua nếu collector không online
            if (collector.getStatus() == null || collector.getStatus() != CollectorStatus.ONLINE) {
                continue;
            }

            // 2. Bỏ qua nếu bị suspend (đã bao gồm ở trên nhưng giữ lại nếu cần check riêng sau này, nhưng hiện tại bỏ qua)
            
            // 3. Bỏ qua collector vừa từ chối (nếu có)
            if (lastRejectedCollectorId != null && lastRejectedCollectorId.equals(collector.getId())) {
                continue;
            }

            // 4. Kiểm tra online (có cập nhật vị trí trong 15 phút gần đây)
            boolean isOnline = false;
            if (collector.getLastLocationUpdate() != null) {
                long minutesDiff = Duration.between(collector.getLastLocationUpdate(), now).toMinutes();
                if (minutesDiff <= 15) {
                    isOnline = true;
                }
            }

            // 5. Đếm số lượng việc đang làm
            long assignedCount = collectionRequestRepository.countByCollector_IdAndStatus(collector.getId(), CollectionRequestStatus.ASSIGNED);
            long acceptedCount = collectionRequestRepository.countByCollector_IdAndStatus(collector.getId(), CollectionRequestStatus.ACCEPTED_COLLECTOR);
            long onTheWayCount = collectionRequestRepository.countByCollector_IdAndStatus(collector.getId(), CollectionRequestStatus.ON_THE_WAY);
            int activeTaskCount = (int) (assignedCount + acceptedCount + onTheWayCount);

            // Tạo đối tượng kết quả
            EligibleCollectorResponse response = new EligibleCollectorResponse();
            response.setId(collector.getId());
            response.setFullName(collector.getFullName());
            response.setStatus(collector.getStatus() != null ? collector.getStatus().name() : null);
            response.setDistanceKm(null); // Chưa tính khoảng cách
            response.setOnline(isOnline);
            response.setActiveTaskCount(activeTaskCount);

            result.add(response);
        }

        // Sắp xếp danh sách: Online lên trước, sau đó đến ít việc hơn
        result.sort((c1, c2) -> {
            // So sánh online
            boolean o1 = Boolean.TRUE.equals(c1.getOnline());
            boolean o2 = Boolean.TRUE.equals(c2.getOnline());
            if (o1 != o2) {
                return o1 ? -1 : 1; // Online true đứng trước
            }
            // So sánh số lượng việc (ít việc hơn đứng trước)
            return Integer.compare(c1.getActiveTaskCount(), c2.getActiveTaskCount());
        });

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RequestPreviewResponse getRequestPreview(Integer enterpriseId, Integer requestId) {
        requireEnterpriseId(enterpriseId);
        requireRequestId(requestId);
        CollectionRequest request = requireEnterpriseRequest(enterpriseId, requestId);
        WasteReport report = request.getReport();
        LocalDateTime created = request.getCreatedAt();
        int sla = computeSlaHours(report != null ? report.getWasteType() : null);
        LocalDateTime due = created != null ? created.plusHours(sla) : null;
        Long remaining = null;
        if (due != null) {
            remaining = Duration.between(LocalDateTime.now(), due).toHours();
        }
        return RequestPreviewResponse.builder()
                .id(request.getId())
                .requestCode(request.getRequestCode())
                .latitude(report != null ? report.getLatitude() : null)
                .longitude(report != null ? report.getLongitude() : null)
                .createdAt(created)
                .wasteType(report != null ? report.getWasteType() : null)
                .slaHours(sla)
                .dueAt(due)
                .hoursRemaining(remaining)
                .priority("FCFS")
                .build();
    }

    private int computeSlaHours(String wasteType) {
        if (wasteType == null) {
            return 72;
        }
        String code = wasteType.trim().toUpperCase();
        return switch (code) {
            case "HAZARDOUS" -> 24;
            case "HOUSEHOLD" -> 48;
            case "RECYCLABLE" -> 72;
            default -> 72;
        };
    }

    private static void requireEnterpriseId(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
    }

    private static void requireRequestId(Integer requestId) {
        if (requestId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu request_id");
        }
    }

    private static void requireCollectorId(Integer collectorId) {
        if (collectorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu collector_id");
        }
    }

    private static void requireReportCode(String reportCode) {
        if (reportCode == null || reportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu report_code");
        }
    }

    private Collector requireEnterpriseCollector(Integer enterpriseId, Integer collectorId) {
        Collector collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collector không tồn tại"));
        if (collector.getEnterprise() == null || collector.getEnterprise().getId() == null
                || !collector.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collector không thuộc doanh nghiệp");
        }
        return collector;
    }

    private static void validateCollectorAssignable(Collector collector) {
        if (collector.getStatus() == null || collector.getStatus() != CollectorStatus.ONLINE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collector không ở trạng thái online");
        }
    }

    private CollectionRequest requireEnterpriseRequest(Integer enterpriseId, Integer requestId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));
        if (request.getEnterprise() == null || request.getEnterprise().getId() == null
                || !request.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc doanh nghiệp");
        }
        return request;
    }

    private ResponseStatusException explainAssignFailure(Integer enterpriseId, Integer requestId) {
        CollectionRequest request = requireEnterpriseRequest(enterpriseId, requestId);
        if (request.getCollector() != null) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection Request đã được gán collector");
        }
        if (request.getStatus() != CollectionRequestStatus.ACCEPTED_ENTERPRISE
                && request.getStatus() != CollectionRequestStatus.REASSIGN) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Collection Request không ở trạng thái hợp lệ để phân công (ACCEPTED_ENTERPRISE/REASSIGN)");
        }
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể phân công Collection Request");
    }

}

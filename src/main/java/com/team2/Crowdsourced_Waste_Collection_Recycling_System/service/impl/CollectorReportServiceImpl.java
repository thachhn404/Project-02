package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ReportCollectorItemResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ReportCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectorReportServiceImpl implements CollectorReportService {

    private final CollectorReportRepository collectorReportRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final WasteReportItemRepository wasteReportItemRepository;
    private final ReportImageRepository reportImageRepository;

    /**
     * Lấy báo cáo theo ID của yêu cầu thu gom.
     */
    @Override
    public CollectorReportResponse getReportByCollectionRequest(Integer requestId, Integer collectorId) {
        // Tìm báo cáo trong DB
        Optional<CollectorReport> reportOpt = collectorReportRepository.findByCollectionRequest_Id(requestId);
        
        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy báo cáo cho request ID: " + requestId);
        }
        
        CollectorReport report = reportOpt.get();
        
        // Kiểm tra quyền truy cập
        if (!report.getCollector().getId().equals(collectorId)) {
             throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return mapToResponse(report);
    }

    /**
     * Lấy danh sách tất cả báo cáo của một Collector.
     */
    @Override
    public List<CollectorReportResponse> getReportsByCollector(Integer collectorId) {
        List<CollectorReport> reports = collectorReportRepository.findByCollector_IdOrderByCreatedAtDesc(collectorId);
        List<CollectorReportResponse> responseList = new ArrayList<>();

        // Duyệt qua từng báo cáo và chuyển đổi sang DTO
        for (CollectorReport report : reports) {
            responseList.add(mapToResponse(report));
        }
        
        return responseList;
    }

    /**
     * Lấy chi tiết một báo cáo theo ID.
     */
    @Override
    public CollectorReportResponse getReportById(Integer reportId, Integer collectorId) {
        Optional<CollectorReport> reportOpt = collectorReportRepository.findById(reportId);
        
        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy báo cáo với ID: " + reportId);
        }

        CollectorReport report = reportOpt.get();

        if (!report.getCollector().getId().equals(collectorId)) {
             throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return mapToResponse(report);
    }

    /**
     * Lấy dữ liệu cần thiết để hiển thị màn hình tạo báo cáo.
     * Bao gồm thông tin request gốc, danh sách rác dự kiến, v.v.
     */
    @Override
    public ReportCollectorResponse getCreateReport(Integer requestId, Integer collectorId) {
        // 1. Tìm Collection Request
        Optional<CollectionRequest> requestOpt = collectionRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại");
        }
        CollectionRequest request = requestOpt.get();

        // 2. Kiểm tra quyền sở hữu
        if (request.getCollector() == null || request.getCollector().getId() == null
                || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }

        // 3. Lấy Waste Report gốc từ người dân
        WasteReport report = request.getReport();
        if (report == null || report.getId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Collection Request không có Waste Report hợp lệ");
        }

        // 4. Lấy danh sách các loại rác trong report gốc
        List<WasteReportItem> items = wasteReportItemRepository.findWithCategoryByReportId(report.getId());
        List<ReportCollectorItemResponse> itemResponses = new ArrayList<>();
        
        for (WasteReportItem i : items) {
            ReportCollectorItemResponse itemDto = new ReportCollectorItemResponse();
            itemDto.setCategoryId(i.getWasteCategory().getId());
            itemDto.setCategoryName(i.getWasteCategory().getName());
            itemDto.setWasteUnit(i.getUnitSnapshot());
            itemDto.setSuggestedQuantity(i.getQuantity());
            
            itemResponses.add(itemDto);
        }

        // 5. Lấy ảnh của report gốc
        List<ReportImage> reportImages = reportImageRepository.findByReport_Id(report.getId());
        List<String> imageUrls = new ArrayList<>();
        for (ReportImage img : reportImages) {
            imageUrls.add(img.getImageUrl());
        }

        // 6. Tạo kết quả trả về
        ReportCollectorResponse response = new ReportCollectorResponse();
        response.setCollectionRequestId(request.getId());
        response.setWasteCollectionRequestId(report.getId());
        response.setWasteReportCode(report.getReportCode());
        response.setWasteType(report.getWasteType());
        response.setAddress(report.getAddress());
        response.setLatitude(report.getLatitude());
        response.setLongitude(report.getLongitude());
        response.setItems(itemResponses);
        response.setImageUrls(imageUrls);

        return response;
    }

    /**
     * Hàm hỗ trợ chuyển đổi từ Entity sang DTO Response.
     */
    private CollectorReportResponse mapToResponse(CollectorReport report) {
        CollectorReportResponse dto = new CollectorReportResponse();
        dto.setId(report.getId());
        dto.setReportCode(report.getReportCode());
        dto.setCollectionRequestId(report.getCollectionRequest().getId());
        dto.setCollectorId(report.getCollector().getId());
        dto.setStatus(report.getStatus());
        dto.setCollectorNote(report.getCollectorNote());
        dto.setTotalPoint(report.getTotalPoint());
        dto.setCollectedAt(report.getCollectedAt());
        dto.setLatitude(report.getLatitude());
        dto.setLongitude(report.getLongitude());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setImageUrls(new ArrayList<>()); // Mặc định list rỗng, có thể bổ sung logic lấy ảnh sau
        
        return dto;
    }
}

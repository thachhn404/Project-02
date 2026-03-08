package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseRequestReportDetailResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseReportDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnterpriseReportDetailServiceImpl implements EnterpriseReportDetailService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorReportRepository collectorReportRepository;
    private final CollectorReportImageRepository collectorReportImageRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final ReportImageRepository reportImageRepository;
    private final WasteReportItemRepository wasteReportItemRepository;

    @Override
    public EnterpriseRequestReportDetailResponse getRequestReportDetail(Integer enterpriseId, Integer requestId) {
        // Kiểm tra quyền truy cập
        requireEnterpriseId(enterpriseId);
        CollectionRequest request = requireOwnedRequest(enterpriseId, requestId);

        // Lấy thông tin WasteReport
        WasteReport report = request.getReport();
        EnterpriseWasteReportResponse wasteReportResponse = null;

        if (report != null) {
            // Lấy danh sách ảnh
            List<String> wasteReportImageUrls = new java.util.ArrayList<>();
            List<ReportImage> images = reportImageRepository.findByReport_Id(report.getId());
            for (ReportImage img : images) {
                wasteReportImageUrls.add(img.getImageUrl());
            }

            // Lấy danh sách danh mục rác
            List<WasteReportItem> reportItems = wasteReportItemRepository.findWithCategoryByReportId(report.getId());
            List<WasteCategoryResponse> wasteReportCategories = toWasteCategoryResponses(reportItems);

            // Tạo response cho WasteReport
            wasteReportResponse = new EnterpriseWasteReportResponse();
            wasteReportResponse.setId(report.getId());
            wasteReportResponse.setReportCode(report.getReportCode());
            wasteReportResponse.setStatus(report.getStatus() != null ? report.getStatus().name() : null);
            wasteReportResponse.setSubmitBy(resolveSubmitBy(report));
            wasteReportResponse.setWasteType(report.getWasteType());
            wasteReportResponse.setDescription(report.getDescription());
            wasteReportResponse.setAddress(report.getAddress());
            wasteReportResponse.setLatitude(report.getLatitude());
            wasteReportResponse.setLongitude(report.getLongitude());
            wasteReportResponse.setImages(report.getImages()); // Lưu ý: trường này có thể deprecated nếu dùng imageUrls
            wasteReportResponse.setImageUrls(wasteReportImageUrls);
            wasteReportResponse.setCategories(wasteReportCategories);
            wasteReportResponse.setCreatedAt(report.getCreatedAt());
        }

        // Lấy thông tin CollectorReport (nếu có)
        CollectorReportResponse collectorReportResponse = null;
        CollectorReport collectorReport = collectorReportRepository.findByCollectionRequest_Id(requestId).orElse(null);
        
        if (collectorReport != null) {
            collectorReportResponse = toCollectorReportResponse(collectorReport);
        }

        // Tạo response tổng hợp
        EnterpriseRequestReportDetailResponse response = new EnterpriseRequestReportDetailResponse();
        response.setRequestId(request.getId());
        response.setRequestCode(request.getRequestCode());
        response.setRequestStatus(request.getStatus() != null ? request.getStatus().name() : null);
        response.setAssignedAt(request.getAssignedAt());
        response.setAcceptedAt(request.getAcceptedAt());
        response.setStartedAt(request.getStartedAt());
        response.setCollectedAt(request.getCollectedAt());
        response.setCompletedAt(request.getCompletedAt());
        response.setActualWeightKg(request.getActualWeightKg());
        response.setWasteReport(wasteReportResponse);
        response.setCollectorReport(collectorReportResponse);

        return response;
    }

    private static void requireEnterpriseId(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
    }

    private CollectionRequest requireOwnedRequest(Integer enterpriseId, Integer requestId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Yêu cầu không tồn tại"));
        if (request.getEnterprise() == null || request.getEnterprise().getId() == null
                || !request.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Yêu cầu không tồn tại");
        }
        return request;
    }

    private String resolveSubmitBy(WasteReport report) {
        // Nếu không có thông tin người gửi -> null
        if (report == null || report.getCitizen() == null) {
            return null;
        }

        var citizen = report.getCitizen();
        
        // Ưu tiên 1: Thông tin từ tài khoản User (nếu đã liên kết)
        if (citizen.getUser() != null) {
            String userFullName = citizen.getUser().getFullName();
            if (userFullName != null && !userFullName.isBlank()) {
                return userFullName;
            }
            
            String userEmail = citizen.getUser().getEmail();
            if (userEmail != null && !userEmail.isBlank()) {
                return userEmail;
            }
        }

        // Ưu tiên 2: Thông tin trực tiếp từ bảng Citizen
        if (citizen.getFullName() != null && !citizen.getFullName().isBlank()) {
            return citizen.getFullName();
        }
        
        if (citizen.getEmail() != null && !citizen.getEmail().isBlank()) {
            return citizen.getEmail();
        }

        return null;
    }

    private CollectorReportResponse toCollectorReportResponse(CollectorReport report) {
        return CollectorReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .collectionRequestId(report.getCollectionRequest() != null ? report.getCollectionRequest().getId() : null)
                .collectorId(report.getCollector() != null ? report.getCollector().getId() : null)
                .status(report.getStatus())
                .collectorNote(report.getCollectorNote())
                .totalPoint(report.getTotalPoint())
                .collectedAt(report.getCollectedAt())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .createdAt(report.getCreatedAt())
                .imageUrls(collectorReportImageRepository.findByCollectorReport_Id(report.getId()).stream()
                        .map(i -> i.getImageUrl())
                        .toList())
                .categories(toWasteCategoryResponsesFromCollectorItems(
                        collectorReportItemRepository.findWithCategoryByCollectorReportId(report.getId())
                ))
                .build();
    }

    private List<WasteCategoryResponse> toWasteCategoryResponses(List<WasteReportItem> items) {
        // Sử dụng Map để gộp các item cùng loại rác
        Map<Integer, WasteCategoryResponse> byCategoryId = new LinkedHashMap<>();
        
        for (WasteReportItem item : items) {
            // Bỏ qua nếu không có thông tin loại rác
            if (item.getWasteCategory() == null || item.getWasteCategory().getId() == null) {
                continue;
            }

            Integer categoryId = item.getWasteCategory().getId();
            
            // Nếu chưa có trong map thì thêm mới
            if (!byCategoryId.containsKey(categoryId)) {
                WasteCategoryResponse response = new WasteCategoryResponse();
                response.setId(categoryId);
                response.setName(item.getWasteCategory().getName());
                
                // Xác định đơn vị tính
                String unit = null;
                if (item.getUnitSnapshot() != null) {
                    unit = item.getUnitSnapshot().name();
                } else if (item.getWasteCategory().getUnit() != null) {
                    unit = item.getWasteCategory().getUnit().name();
                }
                response.setUnit(unit);
                
                response.setPointPerUnit(item.getWasteCategory().getPointPerUnit());
                response.setQuantity(item.getQuantity());
                
                byCategoryId.put(categoryId, response);
            } else {
                // Nếu đã có thì cộng dồn số lượng
                WasteCategoryResponse existing = byCategoryId.get(categoryId);
                if (existing.getQuantity() == null) {
                    existing.setQuantity(item.getQuantity());
                } else if (item.getQuantity() != null) {
                    existing.setQuantity(existing.getQuantity().add(item.getQuantity()));
                }
            }
        }
        
        // Chuyển Map thành List để trả về
        return new java.util.ArrayList<>(byCategoryId.values());
    }

    private List<WasteCategoryResponse> toWasteCategoryResponsesFromCollectorItems(List<CollectorReportItem> items) {
        // Tương tự hàm trên nhưng cho CollectorReportItem
        Map<Integer, WasteCategoryResponse> byCategoryId = new LinkedHashMap<>();
        
        for (CollectorReportItem item : items) {
            if (item.getWasteCategory() == null || item.getWasteCategory().getId() == null) {
                continue;
            }

            Integer categoryId = item.getWasteCategory().getId();
            
            if (!byCategoryId.containsKey(categoryId)) {
                WasteCategoryResponse response = new WasteCategoryResponse();
                response.setId(categoryId);
                response.setName(item.getWasteCategory().getName());
                response.setUnit(item.getUnitSnapshot() != null ? item.getUnitSnapshot().name() : null);
                response.setPointPerUnit(item.getPointPerUnitSnapshot());
                response.setQuantity(item.getQuantity());
                
                byCategoryId.put(categoryId, response);
            } else {
                WasteCategoryResponse existing = byCategoryId.get(categoryId);
                if (existing.getQuantity() == null) {
                    existing.setQuantity(item.getQuantity());
                } else if (item.getQuantity() != null) {
                    existing.setQuantity(existing.getQuantity().add(item.getQuantity()));
                }
            }
        }
        
        return new java.util.ArrayList<>(byCategoryId.values());
    }
}

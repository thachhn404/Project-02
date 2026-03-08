package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.EnterpriseFeedbackResolveRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseFeedbackResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Feedback;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.feedback.FeedbackRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;

@Service
@RequiredArgsConstructor
public class EnterpriseFeedbackServiceImpl implements EnterpriseFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final CollectorReportRepository collectorReportRepository;
    private final CollectorReportImageRepository collectorReportImageRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final CollectorRepository collectorRepository;

    @Override
    public List<EnterpriseFeedbackResponse> getFeedbacks(Integer enterpriseId) {
        // Lấy danh sách feedback từ DB
        List<Feedback> feedbacks = feedbackRepository.findByEnterpriseId(enterpriseId);
        
        // Chuyển đổi sang DTO
        List<EnterpriseFeedbackResponse> responseList = new ArrayList<>();
        for (Feedback fb : feedbacks) {
            responseList.add(toResponseSummary(fb));
        }
        
        return responseList;
    }

    @Override
    public EnterpriseFeedbackResponse getFeedbackDetail(Integer enterpriseId, Integer feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback không tồn tại"));

        // Kiểm tra quyền truy cập của doanh nghiệp
        if (feedback.getCollectionRequest() == null ||
            feedback.getCollectionRequest().getEnterprise() == null ||
            !feedback.getCollectionRequest().getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập feedback này");
        }

        // Lấy thông tin báo cáo thu gom liên quan (nếu có)
        CollectorReportResponse collectorReportResponse = null;
        if (feedback.getCollectionRequest() != null) {
            CollectorReport collectorReport = collectorReportRepository.findByCollectionRequest_Id(feedback.getCollectionRequest().getId())
                    .orElse(null);
            if (collectorReport != null) {
                collectorReportResponse = toCollectorReportResponse(collectorReport);
            }
        }

        return toResponseDetail(feedback, collectorReportResponse);
    }

    @Override
    @Transactional
    public void resolveFeedback(Integer enterpriseId, Integer feedbackId, EnterpriseFeedbackResolveRequest request) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback không tồn tại"));

        // Kiểm tra quyền truy cập
        if (feedback.getCollectionRequest() == null ||
            feedback.getCollectionRequest().getEnterprise() == null ||
            !feedback.getCollectionRequest().getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xử lý feedback này");
        }

        // Cập nhật trạng thái và hướng giải quyết
        feedback.setStatus(request.getStatus()); // RESOLVED (Đã giải quyết) hoặc REJECTED (Từ chối)
        feedback.setResolution(request.getResolution());
        feedbackRepository.save(feedback);

        // Logic xử lý vi phạm của Collector:
        // Nếu khiếu nại là đúng (RESOLVED), tăng số lần vi phạm của Collector lên 1
        if ("RESOLVED".equals(request.getStatus())) {
            if (feedback.getCollectionRequest() != null && feedback.getCollectionRequest().getCollector() != null) {
                Collector collector = feedback.getCollectionRequest().getCollector();
                int currentViolations = collector.getViolationCount() != null ? collector.getViolationCount() : 0;
                
                collector.setViolationCount(currentViolations + 1);
                collectorRepository.save(collector);
            }
        }
    }

    private EnterpriseFeedbackResponse toResponseSummary(Feedback feedback) {
        return EnterpriseFeedbackResponse.builder()
                .id(feedback.getId())
                .feedbackCode(feedback.getFeedbackCode())
                .subject(feedback.getSubject())
                .content(feedback.getContent())
                .resolution(feedback.getResolution())
                .status(feedback.getStatus())
                .citizenName(resolveCitizenName(feedback))
                .citizenEmail(resolveCitizenEmail(feedback))
                .createdAt(feedback.getCreatedAt())
                .collectionRequestId(feedback.getCollectionRequest() != null ? feedback.getCollectionRequest().getId() : null)
                .build();
    }

    private EnterpriseFeedbackResponse toResponseDetail(Feedback feedback, CollectorReportResponse collectorReport) {
        EnterpriseFeedbackResponse response = toResponseSummary(feedback);
        response.setCollectorReport(collectorReport);
        return response;
    }

    private String resolveCitizenName(Feedback feedback) {
        if (feedback.getCitizen() == null) return null;
        if (feedback.getCitizen().getUser() != null && feedback.getCitizen().getUser().getFullName() != null) {
            return feedback.getCitizen().getUser().getFullName();
        }
        return feedback.getCitizen().getFullName();
    }

    private String resolveCitizenEmail(Feedback feedback) {
        if (feedback.getCitizen() == null) return null;
        if (feedback.getCitizen().getUser() != null) {
            return feedback.getCitizen().getUser().getEmail();
        }
        return feedback.getCitizen().getEmail();
    }

    private CollectorReportResponse toCollectorReportResponse(CollectorReport report) {
        // Lấy danh sách ảnh báo cáo của Collector
        List<String> imageUrls = new ArrayList<>();
        var images = collectorReportImageRepository.findByCollectorReport_Id(report.getId());
        for (var img : images) {
            imageUrls.add(img.getImageUrl());
        }

        // Lấy danh sách chi tiết rác thu gom
        List<CollectorReportItem> items = collectorReportItemRepository.findByCollectorReport_Id(report.getId());
        List<WasteCategoryResponse> categories = toWasteCategoryResponsesFromCollectorItems(items);

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
                .imageUrls(imageUrls)
                .categories(categories)
                .build();
    }

    private List<WasteCategoryResponse> toWasteCategoryResponsesFromCollectorItems(List<CollectorReportItem> items) {
        Map<Integer, WasteCategoryResponse> byCategoryId = new LinkedHashMap<>();
        for (CollectorReportItem item : items) {
            if (item.getWasteCategory() == null || item.getWasteCategory().getId() == null) {
                continue;
            }
            Integer categoryId = item.getWasteCategory().getId();
            WasteCategoryResponse existing = byCategoryId.get(categoryId);
            if (existing == null) {
                byCategoryId.put(categoryId, WasteCategoryResponse.builder()
                        .id(categoryId)
                        .name(item.getWasteCategory().getName())
                        .unit(item.getUnitSnapshot() != null ? item.getUnitSnapshot().name() : null)
                        .pointPerUnit(item.getPointPerUnitSnapshot())
                        .quantity(item.getQuantity())
                        .build());
            } else {
                if (existing.getQuantity() == null) {
                    existing.setQuantity(item.getQuantity());
                } else if (item.getQuantity() != null) {
                    existing.setQuantity(existing.getQuantity().add(item.getQuantity()));
                }
            }
        }
        return new ArrayList<>(byCategoryId.values());
    }
}

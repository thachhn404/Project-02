package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateComplaintRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenLeaderboardResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenRewardHistoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ComplaintResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Feedback;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointTransaction;


import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenReportResultItemResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenReportResultResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.mapper.CitizenFeatureMapper;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.feedback.FeedbackRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class WasteReportServiceImpl implements WasteReportService {
    private static final BigDecimal DUP_LAT_DELTA = new BigDecimal("0.00030");
    private static final BigDecimal DUP_LNG_DELTA = new BigDecimal("0.00030");
    private static final List<String> CITIZEN_CATEGORY_ORDER = List.of(
            "Giấy",
            "Báo",
            "Giấy, hồ sơ",
            "Giấy tập",
            "Lon bia",
            "Sắt",
            "Sắt lon",
            "Inox",
            "Đồng",
            "Nhôm",
            "Chai thủy tinh",
            "Bao bì, hỗn hợp",
            "Meca",
            "Mủ",
            "Mủ bình",
            "Mủ tôn",
            "Mủ đen"
    );

    private final WasteReportRepository wasteReportRepository;
    private final CitizenRepository citizenRepository;
    private final WasteCategoryRepository wasteCategoryRepository;
    private final WasteReportItemRepository wasteReportItemRepository;
    private final ReportImageRepository reportImageRepository;
    private final CloudinaryService cloudinaryService;
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorReportRepository collectorReportRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final PointTransactionRepository pointTransactionRepository;

    private final FeedbackRepository feedbackRepository;
    private final CitizenFeatureMapper citizenFeatureMapper;

    @Override
    @Transactional
    public WasteReportResponse createReport(CreateWasteReportRequest request, String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.CITIZEN_NOT_FOUND));

        validateCreateRequest(request);

        BigDecimal latitude = BigDecimal.valueOf(request.getLatitude()).setScale(8, RoundingMode.HALF_UP);
        BigDecimal longitude = BigDecimal.valueOf(request.getLongitude()).setScale(8, RoundingMode.HALF_UP);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        long todayReports = wasteReportRepository.countByCitizen_IdAndCreatedAtBetween(
                citizen.getId(), startOfDay, endOfDay);

        LocalDateTime now = LocalDateTime.now();
        long nearDup = wasteReportRepository.countRecentNearDuplicate(
                citizen.getId(),
                now.minusMinutes(10),
                now,
                latitude,
                longitude,
                DUP_LAT_DELTA,
                DUP_LNG_DELTA);
        if (nearDup > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        WasteReport report = new WasteReport();
        report.setCitizen(citizen);
        report.setWasteType(request.getWasteType().trim().toUpperCase());
        report.setDescription(request.getDescription());
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setAddress(request.getAddress());
        report.setStatus(WasteReportStatus.PENDING);

        String reportCode = "WR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
        report.setReportCode(reportCode);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        WasteReport saved = wasteReportRepository.save(report);

        List<Integer> categoryIds = resolveCategoryIds(request.getCategoryIds());
        List<BigDecimal> quantities = request.getQuantities();
        List<WasteCategory> categories = wasteCategoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (quantities == null || quantities.size() != categoryIds.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<WasteReportItem> reportItems = new ArrayList<>();
        Map<Integer, WasteCategory> categoriesById = categories.stream().collect(Collectors.toMap(WasteCategory::getId, c -> c));
        for (int i = 0; i < categoryIds.size(); i += 1) {
            Integer categoryId = categoryIds.get(i);
            WasteCategory category = categoriesById.get(categoryId);
            if (category == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            WasteReportItem item = new WasteReportItem();
            item.setReport(saved);
            item.setWasteCategory(category);
            BigDecimal quantity = quantities.get(i);
            if (quantity == null || quantity.signum() <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            item.setQuantity(quantity.setScale(4, RoundingMode.HALF_UP));
            item.setUnitSnapshot(category.getUnit());
            item.setCreatedAt(now);
            reportItems.add(item);
        }
        wasteReportItemRepository.saveAll(reportItems);

        saveReportImages(saved, request.getImages(), now);

        return WasteReportResponse.builder()
                .id(saved.getId())
                .message("Tạo báo cáo thành công")
                .reportCode(saved.getReportCode())
                .status(mapCitizenStatus(saved.getStatus()))
                .wasteType(saved.getWasteType())
                .createdAt(saved.getCreatedAt())
                .categories(categories.stream().map(this::toCategoryResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public WasteReportResponse updateReport(Integer reportId, UpdateWasteReportRequest request, String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WasteReport report = wasteReportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_REPORT_NOT_FOUND));

        if (report.getCitizen() == null || !citizen.getId().equals(report.getCitizen().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (report.getStatus() != WasteReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được chỉnh sửa khi status = PENDING");
        }

        if (request.getWasteType() != null && !request.getWasteType().isBlank()) {
            report.setWasteType(request.getWasteType().trim().toUpperCase());
        }
        if (request.getDescription() != null) {
            report.setDescription(request.getDescription());
        }
        if (request.getLatitude() != null) {
            report.setLatitude(BigDecimal.valueOf(request.getLatitude()).setScale(8, RoundingMode.HALF_UP));
        }
        if (request.getLongitude() != null) {
            report.setLongitude(BigDecimal.valueOf(request.getLongitude()).setScale(8, RoundingMode.HALF_UP));
        }
        if (request.getAddress() != null) {
            report.setAddress(request.getAddress());
        }

        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        WasteReport saved = wasteReportRepository.save(report);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Integer> categoryIds = resolveCategoryIds(request.getCategoryIds());
            List<BigDecimal> quantities = request.getQuantities();
            if (quantities == null || quantities.size() != categoryIds.size()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            List<WasteCategory> categories = wasteCategoryRepository.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            wasteReportItemRepository.deleteByReport_Id(saved.getId());
            List<WasteReportItem> reportItems = new ArrayList<>();
            Map<Integer, WasteCategory> categoriesById = categories.stream().collect(Collectors.toMap(WasteCategory::getId, c -> c));
            for (int i = 0; i < categoryIds.size(); i += 1) {
                Integer categoryId = categoryIds.get(i);
                WasteCategory category = categoriesById.get(categoryId);
                if (category == null) {
                    throw new AppException(ErrorCode.INVALID_REQUEST);
                }
                WasteReportItem item = new WasteReportItem();
                item.setReport(saved);
                item.setWasteCategory(category);
                BigDecimal quantity = quantities.get(i);
                if (quantity == null || quantity.signum() <= 0) {
                    throw new AppException(ErrorCode.INVALID_REQUEST);
                }
                item.setQuantity(quantity.setScale(4, RoundingMode.HALF_UP));
                item.setUnitSnapshot(category.getUnit());
                item.setCreatedAt(now);
                reportItems.add(item);
            }
            wasteReportItemRepository.saveAll(reportItems);
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            reportImageRepository.deleteByReport_Id(saved.getId());
            saveReportImages(saved, request.getImages(), now);
        }

        return WasteReportResponse.builder()
                .id(saved.getId())
                .message("Cập nhật báo cáo thành công")
                .reportCode(saved.getReportCode())
                .status(mapCitizenStatus(saved.getStatus()))
                .wasteType(saved.getWasteType())
                .createdAt(saved.getCreatedAt())
                .categories(getCategoriesForReport(saved.getId()))
                .build();
    }

    @Override
    @Transactional
    public void deleteReport(Integer reportId, String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WasteReport report = wasteReportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_REPORT_NOT_FOUND));

        if (report.getCitizen() == null || !citizen.getId().equals(report.getCitizen().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (report.getStatus() != WasteReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được huỷ khi status = PENDING");
        }
        if (collectionRequestRepository.existsByReport_Id(report.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report đã được xử lý, không thể huỷ");
        }

        reportImageRepository.deleteByReport_Id(report.getId());
        wasteReportItemRepository.deleteByReport_Id(report.getId());
        wasteReportRepository.delete(report);
    }

    @Override
    public List<WasteReportResponse> getMyReports(String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<WasteReport> reports = wasteReportRepository.findByCitizen_Id(citizen.getId());
        List<Integer> reportIds = reports.stream().map(WasteReport::getId).toList();
        Map<Integer, List<WasteCategoryResponse>> categoriesByReportId = reportIds.isEmpty()
                ? Map.of()
                : wasteReportItemRepository.findWithCategoryByReportIdIn(reportIds).stream()
                .collect(Collectors.groupingBy(
                        i -> i.getReport().getId(),
                        Collectors.mapping(i -> toCategoryResponse(i.getWasteCategory()), Collectors.toList())
                ));

        return reports.stream()
                .map(report -> WasteReportResponse.builder()
                        .id(report.getId())
                        .reportCode(report.getReportCode())
                        .status(mapCitizenStatus(report.getStatus()))
                        .wasteType(report.getWasteType())
                        .createdAt(report.getCreatedAt())
                        .categories(categoriesByReportId.getOrDefault(report.getId(), List.of()))
                        .build())
                .toList();
    }

    @Override
    public WasteReportResponse getMyReportById(Integer reportId, String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WasteReport report = wasteReportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_REPORT_NOT_FOUND));

        if (report.getCitizen() == null || !citizen.getId().equals(report.getCitizen().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return WasteReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .status(mapCitizenStatus(report.getStatus()))
                .wasteType(report.getWasteType())
                .createdAt(report.getCreatedAt())
                .categories(getCategoriesForReport(report.getId()))
                .build();
    }

    @Override
    public CitizenReportResultResponse getMyReportResult(Integer reportId, String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WasteReport report = wasteReportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_REPORT_NOT_FOUND));

        if (report.getCitizen() == null || !citizen.getId().equals(report.getCitizen().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        CollectionRequest request = collectionRequestRepository.findByReport_Id(report.getId()).orElse(null);
        if (request == null) {
            return CitizenReportResultResponse.builder()
                    .reportId(report.getId())
                    .reportCode(report.getReportCode())
                    .status(mapCitizenStatus(report.getStatus()))
                    .totalPoint(0)
                    .collectedAt(null)
                    .items(List.of())
                    .build();
        }

        CollectorReport collectorReport = collectorReportRepository.findByCollectionRequestId(request.getId()).orElse(null);
        if (collectorReport == null) {
            return CitizenReportResultResponse.builder()
                    .reportId(report.getId())
                    .reportCode(report.getReportCode())
                    .status(mapCitizenStatus(report.getStatus()))
                    .totalPoint(0)
                    .collectedAt(request.getCollectedAt())
                    .items(List.of())
                    .build();
        }

        List<CollectorReportItem> items = collectorReportItemRepository.findByCollectorReportId(collectorReport.getId());
        List<CitizenReportResultItemResponse> itemResponses = items.stream()
                .map(i -> CitizenReportResultItemResponse.builder()
                        .categoryId(i.getWasteCategory().getId())
                        .categoryName(i.getWasteCategory().getName())
                        .quantity(i.getQuantity())
                        .unit(i.getUnitSnapshot() != null ? i.getUnitSnapshot().name() : null)
                        .point(i.getTotalPoint())
                        .build())
                .toList();

        Integer totalPoint = collectorReport.getTotalPoint() != null ? collectorReport.getTotalPoint() : 0;
        if (totalPoint == 0) {
            var txs = pointTransactionRepository.findByCollectionRequestId(request.getId());
            if (!txs.isEmpty()) {
                totalPoint = txs.getLast().getPoints();
            }
        }

        String classificationResult = totalPoint > 0 ? "CORRECT" : "INCORRECT";

        return CitizenReportResultResponse.builder()
                .reportId(report.getId())
                .reportCode(report.getReportCode())
                .status(mapCitizenStatus(report.getStatus()))
                .totalPoint(totalPoint)
                .classificationResult(classificationResult)
                .collectedAt(collectorReport.getCollectedAt())
                .items(itemResponses)
                .build();
    }

    @Override
    public List<CitizenRewardHistoryResponse> getRewardHistory(String citizenEmail, LocalDateTime startDate, LocalDateTime endDate) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<PointTransaction> transactions = pointTransactionRepository.findByCitizenId(citizen.getId());

        if (startDate != null && endDate != null) {
            transactions = transactions.stream()
                    .filter(tx -> {
                        LocalDateTime created = tx.getCreatedAt();
                        return (created.isEqual(startDate) || created.isAfter(startDate)) &&
                                (created.isEqual(endDate) || created.isBefore(endDate));
                    })
                    .collect(Collectors.toList());
        }

        return transactions.stream()
                .map(citizenFeatureMapper::toCitizenRewardHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitizenLeaderboardResponse> getLeaderboard(String region) {
        // Simple implementation: fetch all citizens in region, sort by totalPoints
        // For larger scale, should use a dedicated Leaderboard entity updated by batch jobs
        List<Citizen> citizens = citizenRepository.findAll().stream()
                .filter(c -> region == null || region.isBlank() ||
                        (c.getWard() != null && c.getWard().equalsIgnoreCase(region)) ||
                        (c.getCity() != null && c.getCity().equalsIgnoreCase(region)))
                .sorted((c1, c2) -> {
                    int p1 = c1.getTotalPoints() != null ? c1.getTotalPoints() : 0;
                    int p2 = c2.getTotalPoints() != null ? c2.getTotalPoints() : 0;
                    // Sort desc, if equal points, sort by id (asc) as tie-breaker (older first)
                    if (p1 != p2) return p2 - p1;
                    return c1.getId() - c2.getId();
                })
                .collect(Collectors.toList());

        List<CitizenLeaderboardResponse> leaderboard = new ArrayList<>();
        int rank = 1;
        for (Citizen c : citizens) {
            CitizenLeaderboardResponse resp = citizenFeatureMapper.toCitizenLeaderboardResponse(c);
            resp.setRank(rank++);
            leaderboard.add(resp);
        }
        return leaderboard;
    }

    @Override
    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request, String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WasteReport report = wasteReportRepository.findById(request.getReportId())
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_REPORT_NOT_FOUND));

        if (!report.getCitizen().getId().equals(citizen.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Feedback feedback = citizenFeatureMapper.toFeedback(request);
        feedback.setCitizen(citizen);
        feedback.setFeedbackCode("FB-" + System.currentTimeMillis());
        
        CollectionRequest collectionRequest = collectionRequestRepository.findByReport_Id(report.getId()).orElse(null);
        feedback.setCollectionRequest(collectionRequest);
        
        Feedback saved = feedbackRepository.save(feedback);

        return citizenFeatureMapper.toComplaintResponse(saved);
    }

    @Override
    public List<ComplaintResponse> getComplaints(String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return feedbackRepository.findAll().stream()
                .filter(f -> f.getCitizen().getId().equals(citizen.getId()))
                .map(citizenFeatureMapper::toComplaintResponse)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<WasteCategoryResponse> getWasteCategories() {
        return wasteCategoryRepository.findAll().stream()
                .map(c -> new Object[] { c, findOrderIndex(c.getName()) })
                .filter(arr -> arr[1] != null)
                .sorted((a, b) -> ((Integer) a[1]).compareTo((Integer) b[1]))
                .map(arr -> (WasteCategory) arr[0])
                .map(c -> WasteCategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .unit(c.getUnit() != null ? c.getUnit().name() : null)
                        .pointPerUnit(c.getPointPerUnit())
                        .build())
                .toList();
    }

    private List<WasteCategoryResponse> getCategoriesForReport(Integer reportId) {
        return wasteReportItemRepository.findWithCategoryByReportId(reportId).stream()
                .map(WasteReportItem::getWasteCategory)
                .filter(Objects::nonNull)
                .map(this::toCategoryResponse)
                .distinct()
                .toList();
    }

    private WasteCategoryResponse toCategoryResponse(WasteCategory category) {
        return WasteCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .unit(category.getUnit() != null ? category.getUnit().name() : null)
                .pointPerUnit(category.getPointPerUnit())
                .build();
    }

    private void validateCreateRequest(CreateWasteReportRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        for (MultipartFile image : request.getImages()) {
            if (image == null || image.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            String contentType = image.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getWasteType() == null || request.getWasteType().isBlank()) {
            throw new AppException(ErrorCode.WASTE_TYPE_NOT_FOUND);
        }
        if (request.getCategoryIds() == null || request.getCategoryIds().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getQuantities() == null || request.getQuantities().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getQuantities().size() != request.getCategoryIds().size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        for (BigDecimal q : request.getQuantities()) {
            if (q == null || q.signum() <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

    private void saveReportImages(WasteReport report, List<MultipartFile> images, LocalDateTime now) {
        boolean first = true;
        for (MultipartFile file : images) {
            CloudinaryResponse uploaded = cloudinaryService.uploadImage(file, "reports");
            if (uploaded == null || uploaded.getUrl() == null) {
                throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
            }

            if (first) {
                report.setImages(uploaded.getUrl());
                report.setCloudinaryPublicId(uploaded.getPublicId());
                wasteReportRepository.save(report);
                first = false;
            }

            ReportImage img = new ReportImage();
            img.setReport(report);
            img.setImageUrl(uploaded.getUrl());
            img.setImageType("BEFORE");
            img.setUploadedAt(now);
            reportImageRepository.save(img);
        }
    }

    private String mapCitizenStatus(WasteReportStatus status) {
        if (status == null) {
            return "PENDING";
        }
        return switch (status) {
            case PENDING -> "PENDING";
            case ACCEPTED_ENTERPRISE -> "ACCEPTED";
            case ASSIGNED, ACCEPTED_COLLECTOR, ON_THE_WAY -> "ASSIGNED";
            case COLLECTED -> "COLLECTED";
            case REJECTED -> "REJECTED";
            case TIMED_OUT -> "TIMED_OUT";
        };
    }

    private List<Integer> resolveCategoryIds(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<Integer> ids = new ArrayList<>();
        for (String raw : rawValues) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String[] parts = raw.split(",");
            for (String part : parts) {
                String token = part != null ? part.trim() : null;
                if (token == null || token.isEmpty()) {
                    continue;
                }
                Integer id = tryParseInt(token);
                if (id != null) {
                    ids.add(id);
                    continue;
                }
                Integer resolved = wasteCategoryRepository.findByNameIgnoreCase(token)
                        .map(WasteCategory::getId)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
                ids.add(resolved);
            }
        }

        List<Integer> deduped = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (deduped.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return deduped;
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer findOrderIndex(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        OptionalInt index = OptionalInt.empty();
        for (int i = 0; i < CITIZEN_CATEGORY_ORDER.size(); i++) {
            if (CITIZEN_CATEGORY_ORDER.get(i).toLowerCase(Locale.ROOT).equals(normalized)) {
                index = OptionalInt.of(i);
                break;
            }
        }
        return index.isPresent() ? index.getAsInt() : null;
    }
}

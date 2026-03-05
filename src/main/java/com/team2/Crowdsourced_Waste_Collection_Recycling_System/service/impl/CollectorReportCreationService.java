package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.WorkRuleProperties;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointTransaction;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectorReportCreationService {

    private static final String EARN_TRANSACTION_TYPE = "EARN";
    private static final String EARN_DESCRIPTION = "Điểm thưởng thu gom";

    private final CollectorReportRepository collectorReportRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final WasteCategoryRepository wasteCategoryRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final CollectorReportImageRepository collectorReportImageRepository;
    private final CitizenRepository citizenRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final WasteReportRepository wasteReportRepository;
    private final CloudinaryService cloudinaryService;
    private final WorkRuleProperties workRuleProperties;

    @Transactional
    public CollectorReportResponse createCollectorReport(Integer requestId, Integer collectorId, CreateCollectorReportRequest request) {
        CollectionRequest collectionRequest = getAndValidateCollectionRequest(requestId, collectorId);
        WasteReport wasteReport = getAndValidateWasteReport(collectionRequest);
        validateGpsWithinRadius(wasteReport, request.getLatitude(), request.getLongitude());
        validateInput(request);

        Calculation calculation = calculateItems(request.getCategoryIds(), request.getQuantities(), request.getVerificationRate());
        LocalDateTime now = LocalDateTime.now();

        CollectorReport report = createAndSaveReport(collectionRequest, request, calculation.totalPoints(), now);
        saveItems(report, calculation.items());
        List<String> imageUrls = uploadImages(report, request.getImages(), now);

        confirmCompleted(requestId, collectorId, calculation.totalWeightKg(), now);
        updateWasteReportStatus(wasteReport, now);
        rewardCitizen(collectionRequest, wasteReport, calculation.totalPoints(), now);

        return buildResponse(report, imageUrls, calculation.items());
    }

    private CollectionRequest getAndValidateCollectionRequest(Integer requestId, Integer collectorId) {
        if (requestId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu requestId");
        }
        if (collectorId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Collector");
        }
        if (collectorReportRepository.existsByCollectionRequest_Id(requestId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report đã được tạo cho collection request này");
        }

        CollectionRequest collectionRequest = collectionRequestRepository.findByIdAndCollector_Id(requestId, collectorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        if (collectionRequest.getStatus() != CollectionRequestStatus.COLLECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể tạo report khi task đang ở trạng thái COLLECTED");
        }
        return collectionRequest;
    }

    private WasteReport getAndValidateWasteReport(CollectionRequest collectionRequest) {
        WasteReport wasteReport = collectionRequest.getReport();
        if (wasteReport == null || wasteReport.getLatitude() == null || wasteReport.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection Request thiếu toạ độ ban đầu");
        }
        return wasteReport;
    }

    private void validateGpsWithinRadius(WasteReport wasteReport, Double actualLatitude, Double actualLongitude) {
        double distKm = haversineKm(
                wasteReport.getLatitude().doubleValue(),
                wasteReport.getLongitude().doubleValue(),
                actualLatitude,
                actualLongitude
        );
        if (distKm > workRuleProperties.getReportRadiusKm()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GPS thực tế không nằm gần vị trí ban đầu");
        }
    }

    private void validateInput(CreateCollectorReportRequest request) {
        List<MultipartFile> images = request.getImages();
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần ít nhất 1 ảnh");
        }

        List<Integer> categoryIds = request.getCategoryIds();
        List<BigDecimal> quantities = request.getQuantities();
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phải chọn ít nhất 1 danh mục");
        }
        if (quantities == null || quantities.size() != categoryIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu khối lượng không hợp lệ");
        }
    }

    private Calculation calculateItems(List<Integer> categoryIds, List<BigDecimal> quantities, Integer verificationRate) {
        List<CollectorReportItem> items = new ArrayList<>();
        BigDecimal totalWeightKg = BigDecimal.ZERO;
        int totalPoints = 0;

        for (int i = 0; i < categoryIds.size(); i++) {
            Integer categoryId = categoryIds.get(i);
            BigDecimal quantity = quantities.get(i);

            WasteCategory category = wasteCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID " + categoryId + " không tồn tại"));

            CollectorReportItem item = new CollectorReportItem();
            item.setWasteCategory(category);
            item.setQuantity(quantity);
            item.setUnitSnapshot(category.getUnit());
            item.setPointPerUnitSnapshot(category.getPointPerUnit());

            int points = 0;
            if (category.getPointPerUnit() != null) {
                points = quantity.multiply(category.getPointPerUnit()).intValue();
            }

            if (verificationRate != null) {
                if (verificationRate == 0) {
                    points = 0;
                } else if (verificationRate == 50) {
                    points = points / 2;
                }
            }

            item.setTotalPoint(points);

            items.add(item);
            totalPoints += points;

            if (category.getUnit() == WasteUnit.KG) {
                totalWeightKg = totalWeightKg.add(quantity);
            }
        }

        if (totalWeightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tổng khối lượng phải > 0");
        }

        return new Calculation(items, totalPoints, totalWeightKg);
    }

    private CollectorReport createAndSaveReport(CollectionRequest collectionRequest, CreateCollectorReportRequest request, int totalPoints, LocalDateTime now) {
        CollectorReport report = new CollectorReport();
        report.setCollectionRequest(collectionRequest);
        report.setCollector(collectionRequest.getCollector());
        report.setStatus(CollectorReportStatus.COMPLETED);
        report.setCollectorNote(request.getCollectorNote());
        report.setTotalPoint(totalPoints);
        report.setCollectedAt(now);
        report.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        report.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        report.setCreatedAt(now);

        CollectorReport saved = collectorReportRepository.save(report);
        if (saved.getReportCode() == null || saved.getReportCode().isBlank()) {
            saved.setReportCode(String.format("CRR%06d", saved.getId()));
            saved = collectorReportRepository.save(saved);
        }
        return saved;
    }

    private void saveItems(CollectorReport report, List<CollectorReportItem> items) {
        for (CollectorReportItem item : items) {
            item.setCollectorReport(report);
        }
        collectorReportItemRepository.saveAll(items);
    }

    private List<String> uploadImages(CollectorReport report, List<MultipartFile> images, LocalDateTime now) {
        List<CollectorReportImage> imageEntities = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : images) {
            var uploaded = cloudinaryService.uploadImage(file, "collectorReport");

            CollectorReportImage img = new CollectorReportImage();
            img.setCollectorReport(report);
            img.setImageUrl(uploaded.getUrl());
            img.setImagePublicId(uploaded.getPublicId());
            img.setCreatedAt(now);

            imageEntities.add(img);
            imageUrls.add(uploaded.getUrl());
        }

        collectorReportImageRepository.saveAll(imageEntities);
        return imageUrls;
    }

    private void confirmCompleted(Integer requestId, Integer collectorId, BigDecimal totalWeightKg, LocalDateTime now) {
        BigDecimal scaledWeightKg = totalWeightKg.setScale(2, RoundingMode.HALF_UP);
        int updated = collectionRequestRepository.confirmCompletedWithWeight(requestId, collectorId, scaledWeightKg, now);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xác nhận hoàn tất task");
        }
    }

    private void updateWasteReportStatus(WasteReport wasteReport, LocalDateTime now) {
        wasteReport.setStatus(WasteReportStatus.COLLECTED);
        wasteReport.setUpdatedAt(now);
        wasteReportRepository.save(wasteReport);
    }

    private void rewardCitizen(CollectionRequest collectionRequest, WasteReport wasteReport, int points, LocalDateTime now) {
        if (points <= 0) {
            return;
        }

        Integer citizenId = wasteReport.getCitizen() != null ? wasteReport.getCitizen().getId() : null;
        if (citizenId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Waste report thiếu citizen");
        }

        boolean alreadyRewarded = pointTransactionRepository.existsByCollectionRequestIdAndTransactionType(
                collectionRequest.getId(),
                EARN_TRANSACTION_TYPE
        );
        if (alreadyRewarded) {
            return;
        }

        Citizen citizen = citizenRepository.findByIdForUpdate(citizenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen không tồn tại"));

        if (pointTransactionRepository.existsByCollectionRequestIdAndTransactionType(collectionRequest.getId(), EARN_TRANSACTION_TYPE)) {
            return;
        }

        int currentPoints = citizen.getTotalPoints() == null ? 0 : citizen.getTotalPoints();
        int balanceAfter = currentPoints + points;

        citizen.setTotalPoints(balanceAfter);
        citizenRepository.save(citizen);

        PointTransaction tx = new PointTransaction();
        tx.setCitizen(citizen);
        tx.setReport(wasteReport);
        tx.setCollectionRequest(collectionRequest);
        tx.setPoints(points);
        tx.setTransactionType(EARN_TRANSACTION_TYPE);
        tx.setDescription(EARN_DESCRIPTION);
        tx.setBalanceAfter(balanceAfter);
        tx.setCreatedBy(collectionRequest.getCollector().getUser());
        tx.setCreatedAt(now);
        pointTransactionRepository.save(tx);
    }

    private CollectorReportResponse buildResponse(CollectorReport report, List<String> imageUrls, List<CollectorReportItem> items) {
        List<WasteCategoryResponse> categories = new ArrayList<>();
        for (CollectorReportItem item : items) {
            WasteCategory category = item.getWasteCategory();
            categories.add(WasteCategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .unit(item.getUnitSnapshot().name())
                    .pointPerUnit(item.getPointPerUnitSnapshot())
                    .quantity(item.getQuantity())
                    .build());
        }

        return CollectorReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .collectionRequestId(report.getCollectionRequest().getId())
                .collectorId(report.getCollector().getId())
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

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    private record Calculation(List<CollectorReportItem> items, int totalPoints, BigDecimal totalWeightKg) {
    }
}

package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.WorkRuleProperties;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
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
    private final CollectorReportRepository collectorReportRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final WasteCategoryRepository wasteCategoryRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final CollectorReportImageRepository collectorReportImageRepository;
    private final CloudinaryService cloudinaryService;
    private final WorkRuleProperties workRuleProperties;

    @Transactional
    public CollectorReportResponse createCollectorReport(Integer requestId, Integer collectorId, CreateCollectorReportRequest request) {
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

        WasteReport wasteReport = collectionRequest.getReport();
        if (wasteReport == null || wasteReport.getLatitude() == null || wasteReport.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection Request thiếu toạ độ ban đầu");
        }

        double distKm = haversineKm(
                wasteReport.getLatitude().doubleValue(),
                wasteReport.getLongitude().doubleValue(),
                request.getLatitude(),
                request.getLongitude()
        );
        if (distKm > workRuleProperties.getReportRadiusKm()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GPS thực tế không nằm gần vị trí ban đầu");
        }

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

        BigDecimal totalWeightKg = BigDecimal.ZERO;
        int totalPoints = 0;
        List<CollectorReportItem> itemEntities = new ArrayList<>();
        for (int i = 0; i < categoryIds.size(); i++) {
            Integer categoryId = categoryIds.get(i);
            BigDecimal qty = quantities.get(i);
            if (categoryId == null || qty == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục và khối lượng là bắt buộc");
            }
            if (qty.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khối lượng không được âm");
            }
            if (qty.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            WasteCategory category = wasteCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không tồn tại"));

            WasteUnit unit = category.getUnit();
            BigDecimal pointPerUnit = category.getPointPerUnit() != null ? category.getPointPerUnit() : BigDecimal.ZERO;
            int itemPoints = qty.multiply(pointPerUnit).setScale(0, RoundingMode.HALF_UP).intValue();
            totalPoints += itemPoints;

            totalWeightKg = totalWeightKg.add(qty);

            CollectorReportItem item = new CollectorReportItem();
            item.setWasteCategory(category);
            item.setQuantity(qty);
            item.setUnitSnapshot(unit);
            item.setPointPerUnitSnapshot(pointPerUnit);
            item.setTotalPoint(itemPoints);
            itemEntities.add(item);
        }

        if (totalWeightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tổng khối lượng phải > 0");
        }

        LocalDateTime now = LocalDateTime.now();
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

        for (CollectorReportItem item : itemEntities) {
            item.setCollectorReport(saved);
        }
        collectorReportItemRepository.saveAll(itemEntities);

        List<CollectorReportImage> imageEntities = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : images) {
            var uploaded = cloudinaryService.uploadImage(file, "collectorReport");
            CollectorReportImage img = new CollectorReportImage();
            img.setCollectorReport(saved);
            img.setImageUrl(uploaded.getUrl());
            img.setImagePublicId(uploaded.getPublicId());
            img.setCreatedAt(now);
            imageEntities.add(img);
            imageUrls.add(uploaded.getUrl());
        }
        collectorReportImageRepository.saveAll(imageEntities);

        BigDecimal scaledWeightKg = totalWeightKg.setScale(2, RoundingMode.HALF_UP);
        int updated = collectionRequestRepository.confirmCompletedWithWeight(requestId, collectorId, scaledWeightKg, now);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xác nhận hoàn tất task");
        }

        return CollectorReportResponse.builder()
                .id(saved.getId())
                .reportCode(saved.getReportCode())
                .collectionRequestId(saved.getCollectionRequest().getId())
                .collectorId(saved.getCollector().getId())
                .status(saved.getStatus())
                .collectorNote(saved.getCollectorNote())
                .totalPoint(saved.getTotalPoint())
                .collectedAt(saved.getCollectedAt())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .createdAt(saved.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

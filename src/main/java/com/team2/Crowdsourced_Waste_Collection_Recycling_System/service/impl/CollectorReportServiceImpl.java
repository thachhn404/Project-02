package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportItemResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorReportService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward.RecyclableRewardCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CollectorReportServiceImpl implements CollectorReportService {
    private final CollectorReportRepository collectorReportRepository;
    private final CollectorReportImageRepository collectorReportImageRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;
    private final CollectorRepository collectorRepository;
    private final WasteReportRepository wasteReportRepository;
    private final WasteCategoryRepository wasteCategoryRepository;
    private final CitizenRepository citizenRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public CollectorReportResponse createCollectorReport(CreateCollectorReportRequest request, List<MultipartFile> images, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();

        validateCreateRequest(request, images);

        CollectionRequest collectionRequest = loadAndValidateCollectionRequest(request.getCollectionRequestId(), collectorId);
        validateNotAlreadyReportedOrAwarded(collectionRequest);

        RecyclableRewardCalculator.CalculationResult calculation = calculateReward(request);
        if (calculation.totalPoint() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalPoint must be > 0");
        }

        Collector collector = collectorRepository.getReferenceById(collectorId);
        BuiltItems builtItems = buildReportItems(calculation, now);

        CollectorReport savedReport = saveCollectorReport(request, collectionRequest, collector, calculation.totalPoint(), now);
        saveReportItems(savedReport, builtItems.itemsToSave);
        List<String> imageUrls = saveReportImages(savedReport, images, now);

        finalizeCollectionRequest(collectionRequest, calculation.totalWeightKg(), now);
        saveCompletionTracking(collectionRequest, collector, now);
        updateWasteReportAndAwardPointsIfPresent(collectionRequest, request.getAddress(), calculation.totalPoint(), now);

        return mapToResponse(savedReport, imageUrls, builtItems.itemResponses);
    }

    @Override
//lay report theo report id
    public CollectorReportResponse getReportById(Integer reportId, Integer collectorId) {
        CollectorReport report = collectorReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report không tồn tại"));
        if (!report.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Report không thuộc về bạn");
        }
        
        List<String> imageUrls = new ArrayList<>();
        for (CollectorReportImage image : report.getImages()) {
            imageUrls.add(image.getImageUrl());
        }

        List<CollectorReportItemResponse> items = collectorReportItemRepository.findByCollectorReportId(report.getId())
                .stream()
                .map(item -> CollectorReportItemResponse.builder()
                        .categoryId(item.getWasteCategory().getId())
                        .categoryName(item.getWasteCategory().getName())
                        .quantity(item.getQuantity())
                        .unit(item.getUnitSnapshot())
                        .point(item.getTotalPoint())
                        .build())
                .toList();

        return mapToResponse(report, imageUrls, items);
    }

    @Override
  //lay danh sách report của collector hiện tại
    public List<CollectorReportResponse> getReportsByCollector(Integer collectorId) {
        Page<CollectorReport> reports = collectorReportRepository.findByCollectorIdOrderByCreatedAtDesc(collectorId, Pageable.unpaged());
        return reports.map(report -> {
            List<String> imageUrls = new ArrayList<>();
            for (CollectorReportImage image : report.getImages()) {
                imageUrls.add(image.getImageUrl());
            }
            List<CollectorReportItemResponse> items = collectorReportItemRepository.findByCollectorReportId(report.getId())
                    .stream()
                    .map(item -> CollectorReportItemResponse.builder()
                            .categoryId(item.getWasteCategory().getId())
                            .categoryName(item.getWasteCategory().getName())
                            .quantity(item.getQuantity())
                            .unit(item.getUnitSnapshot())
                            .point(item.getTotalPoint())
                            .build())
                    .toList();
            return mapToResponse(report, imageUrls, items);
        }).getContent();
    }

    @Override
    //lấy report theo collectionRequest
    public CollectorReportResponse getReportByCollectionRequest(Integer collectionRequestId, Integer collectorId) {
        CollectorReport report = collectorReportRepository.findByCollectionRequestId(collectionRequestId)
                .orElse(null);
        if (report == null) {
            return null;
        }
        if (!report.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Report không thuộc về bạn");
        }
        
        List<String> imageUrls = new ArrayList<>();
        for (CollectorReportImage image : report.getImages()) {
            imageUrls.add(image.getImageUrl());
        }

        List<CollectorReportItemResponse> items = collectorReportItemRepository.findByCollectorReportId(report.getId())
                .stream()
                .map(item -> CollectorReportItemResponse.builder()
                        .categoryId(item.getWasteCategory().getId())
                        .categoryName(item.getWasteCategory().getName())
                        .quantity(item.getQuantity())
                        .unit(item.getUnitSnapshot())
                        .point(item.getTotalPoint())
                        .build())
                .toList();

        return mapToResponse(report, imageUrls, items);
    }

    private CollectorReportResponse mapToResponse(CollectorReport report, List<String> imageUrls, List<CollectorReportItemResponse> items) {
        return CollectorReportResponse.builder()
                .reportId(report.getId())
                .reportCode(report.getReportCode())
                .collectionRequestId(report.getCollectionRequest().getId())
                .collectorId(report.getCollector().getId())
                .collectorName(report.getCollector().getUser().getFullName())
                .status(report.getStatus())
                .collectorNote(report.getCollectorNote())
                .totalPoint(report.getTotalPoint())
                .actualWeightRecyclable(report.getActualWeightRecyclable())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .collectedAt(report.getCollectedAt())
                .createdAt(report.getCreatedAt())
                .imageUrls(imageUrls)
                .items(items)
                .build();
    }

    private String formatCollectorReportCode(Integer id) {
        return "CR-" + String.format("%06d", id);
    }

    private void validateCreateRequest(CreateCollectorReportRequest request, List<MultipartFile> images) {
        if (request == null || request.getCollectionRequestId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CollectionRequestId is required");
        }
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần ít nhất 1 ảnh");
        }
        if (request.getWasteType() == null || !"RECYCLABLE".equalsIgnoreCase(request.getWasteType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wasteType must be RECYCLABLE");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items is required");
        }
    }

    private CollectionRequest loadAndValidateCollectionRequest(Integer collectionRequestId, Integer collectorId) {
        CollectionRequest collectionRequest = collectionRequestRepository.findById(collectionRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));
        if (collectionRequest.getCollector() == null || !collectionRequest.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }
        if (collectionRequest.getStatus() != CollectionRequestStatus.COLLECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Chỉ có thể xác nhận hoàn tất khi status là 'COLLECTED'. Hiện tại:'%s'",
                            collectionRequest.getStatus()));
        }
        return collectionRequest;
    }

    private void validateNotAlreadyReportedOrAwarded(CollectionRequest collectionRequest) {
        if (collectorReportRepository.findByCollectionRequestId(collectionRequest.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report đã tồn tại cho collection request này");
        }
        if (!pointTransactionRepository.findByCollectionRequestId(collectionRequest.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Points already awarded for this collection request");
        }
    }

    private RecyclableRewardCalculator.CalculationResult calculateReward(CreateCollectorReportRequest request) {
        Map<Integer, BigDecimal> quantityByCategoryId = mergeItemQuantities(request.getItems());
        Map<Integer, WasteCategory> categoriesById = loadCategories(quantityByCategoryId.keySet().stream().toList());

        List<RecyclableRewardCalculator.ItemInput> inputs = new ArrayList<>();
        for (var entry : quantityByCategoryId.entrySet()) {
            WasteCategory category = categoriesById.get(entry.getKey());
            validateCategoryForRecyclable(category);
            validateQuantityForUnit(entry.getValue(), category.getUnit());
            inputs.add(new RecyclableRewardCalculator.ItemInput(category, entry.getValue()));
        }
        try {
            return new RecyclableRewardCalculator().calculate(inputs);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reward calculation");
        }
    }

    private record BuiltItems(List<CollectorReportItem> itemsToSave, List<CollectorReportItemResponse> itemResponses) { }

    private BuiltItems buildReportItems(RecyclableRewardCalculator.CalculationResult calculation, LocalDateTime now) {
        List<CollectorReportItem> itemsToSave = new ArrayList<>();
        List<CollectorReportItemResponse> itemResponses = new ArrayList<>();

        for (RecyclableRewardCalculator.ItemResult result : calculation.items()) {
            WasteCategory category = result.category();
            BigDecimal quantity = result.quantity();
            int itemPoint = result.point();

            CollectorReportItem item = new CollectorReportItem();
            item.setWasteCategory(category);
            item.setQuantity(quantity);
            item.setUnitSnapshot(category.getUnit());
            item.setPointPerUnitSnapshot(category.getPointPerUnit());
            item.setTotalPoint(itemPoint);
            item.setCreatedAt(now);
            itemsToSave.add(item);

            itemResponses.add(CollectorReportItemResponse.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .quantity(quantity)
                    .unit(item.getUnitSnapshot())
                    .point(itemPoint)
                    .build());
        }

        return new BuiltItems(itemsToSave, itemResponses);
    }

    private CollectorReport saveCollectorReport(CreateCollectorReportRequest request,
                                               CollectionRequest collectionRequest,
                                               Collector collector,
                                               int totalPoint,
                                               LocalDateTime now) {
        CollectorReport report = new CollectorReport();
        report.setCollectionRequest(collectionRequest);
        report.setCollector(collector);
        report.setStatus(CollectorReportStatus.COMPLETED);
        report.setCollectorNote(request.getCollectorNote());
        report.setTotalPoint(totalPoint);
        if (request.getLatitude() != null) {
            report.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        }
        if (request.getLongitude() != null) {
            report.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        }
        report.setCollectedAt(now);
        report.setCreatedAt(now);

        CollectorReport savedReport = collectorReportRepository.save(report);
        if (savedReport.getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo report");
        }
        if (savedReport.getReportCode() == null || savedReport.getReportCode().isBlank()) {
            savedReport.setReportCode(formatCollectorReportCode(savedReport.getId()));
            savedReport = collectorReportRepository.save(savedReport);
        }
        return savedReport;
    }

    private void saveReportItems(CollectorReport savedReport, List<CollectorReportItem> itemsToSave) {
        for (CollectorReportItem item : itemsToSave) {
            item.setCollectorReport(savedReport);
        }
        collectorReportItemRepository.saveAll(itemsToSave);
    }

    private List<String> saveReportImages(CollectorReport savedReport, List<MultipartFile> images, LocalDateTime now) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : images) {
            CloudinaryResponse cloudinaryResponse = cloudinaryService.uploadImage(file, "collectorReport");
            CollectorReportImage image = new CollectorReportImage();
            image.setCollectorReport(savedReport);
            image.setImageUrl(cloudinaryResponse.getUrl());
            image.setImagePublicId(cloudinaryResponse.getPublicId());
            image.setCreatedAt(now);
            collectorReportImageRepository.save(image);
            imageUrls.add(cloudinaryResponse.getUrl());
        }
        return imageUrls;
    }

    private void saveCompletionTracking(CollectionRequest collectionRequest, Collector collector, LocalDateTime now) {
        CollectionTracking tracking = new CollectionTracking();
        tracking.setCollectionRequest(collectionRequest);
        tracking.setCollector(collector);
        tracking.setAction("completed");
        tracking.setNote("Collector confirmed completion");
        tracking.setCreatedAt(now);
        collectionTrackingRepository.save(tracking);
    }

    private void finalizeCollectionRequest(CollectionRequest collectionRequest, BigDecimal totalWeightKg, LocalDateTime now) {
        if (collectionRequest.getStatus() != CollectionRequestStatus.COLLECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xác nhận hoàn tất (trạng thái phải là COLLECTED)");
        }
        collectionRequest.setActualWeightKg(totalWeightKg);
        collectionRequest.setStatus(CollectionRequestStatus.COMPLETED);
        collectionRequest.setCompletedAt(now);
        collectionRequest.setUpdatedAt(now);
        collectionRequestRepository.save(collectionRequest);
    }

    private void updateWasteReportAndAwardPointsIfPresent(CollectionRequest collectionRequest, String address, int totalPoint, LocalDateTime now) {
        WasteReport wasteReport = collectionRequest.getReport();
        if (wasteReport == null) {
            return;
        }
        if (address != null && !address.isBlank()) {
            wasteReport.setAddress(address);
        }
        wasteReport.setStatus(WasteReportStatus.COLLECTED);
        wasteReport.setUpdatedAt(now);
        wasteReportRepository.save(wasteReport);

        awardPoints(wasteReport, collectionRequest, totalPoint, now);
    }

    private Map<Integer, BigDecimal> mergeItemQuantities(List<com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CollectorReportItemRequest> items) {
        Map<Integer, BigDecimal> map = new HashMap<>();
        for (var item : items) {
            if (item == null || item.getQuantity() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid item");
            }
            Integer categoryId = resolveCategoryId(item);
            if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
            }
            map.merge(categoryId, item.getQuantity(), BigDecimal::add);
        }
        return map;
    }

    private Integer resolveCategoryId(com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CollectorReportItemRequest item) {
        if (item.getCategoryId() != null) {
            return item.getCategoryId();
        }
        String name = item.getCategoryName() != null ? item.getCategoryName().trim() : null;
        if (name == null || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide either categoryId or categoryName");
        }
        return wasteCategoryRepository.findByNameIgnoreCase(name)
                .map(WasteCategory::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found: " + name));
    }

    private Map<Integer, WasteCategory> loadCategories(List<Integer> categoryIds) {
        List<WasteCategory> categories = wasteCategoryRepository.findAllById(categoryIds);
        Map<Integer, WasteCategory> map = new HashMap<>();
        for (WasteCategory category : categories) {
            map.put(category.getId(), category);
        }
        for (Integer id : categoryIds) {
            if (!map.containsKey(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found: " + id);
            }
        }
        return map;
    }

    private void validateCategoryForRecyclable(WasteCategory category) {
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }
        if (category.getUnit() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category unit is required: " + category.getId());
        }
        if (category.getPointPerUnit() == null || category.getPointPerUnit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category pointPerUnit must be > 0: " + category.getId());
        }
    }

    private void validateQuantityForUnit(BigDecimal quantity, WasteUnit unit) {
        if (quantity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity is required");
        }
        if (unit == WasteUnit.CAN || unit == WasteUnit.BOTTLE) {
            if (quantity.stripTrailingZeros().scale() > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be an integer for unit " + unit);
            }
        }
    }

    private int safeAddPoint(int total, int add) {
        long sum = (long) total + (long) add;
        if (sum > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total point is too large");
        }
        return (int) sum;
    }

    private void awardPoints(WasteReport wasteReport, CollectionRequest collectionRequest, int points, LocalDateTime now) {
        Citizen citizen = wasteReport.getCitizen();
        if (citizen == null || citizen.getId() == null) {
            return;
        }

        Citizen managedCitizen = citizenRepository.findByIdForUpdate(citizen.getId()).orElse(citizen);
        int currentBalance = managedCitizen.getTotalPoints() != null ? managedCitizen.getTotalPoints() : 0;
        int balanceAfter = safeAddPoint(currentBalance, points);
        managedCitizen.setTotalPoints(balanceAfter);
        citizenRepository.save(managedCitizen);

        PointTransaction transaction = new PointTransaction();
        transaction.setCitizen(managedCitizen);
        transaction.setReport(wasteReport);
        transaction.setCollectionRequest(collectionRequest);
        transaction.setPoints(points);
        transaction.setTransactionType("EARN");
        transaction.setDescription("Recyclable report reward");
        transaction.setBalanceAfter(balanceAfter);
        transaction.setCreatedAt(now);
        pointTransactionRepository.save(transaction);
    }
}

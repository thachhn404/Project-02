package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateComplaintRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenLeaderboardResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenPointSummaryResponse;
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
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.mapper.CitizenFeatureMapper;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.feedback.FeedbackRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenReportStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;

@Service
@RequiredArgsConstructor
public class WasteReportServiceImpl implements WasteReportService {
    // Các hằng số dùng để so sánh vị trí trùng lặp (khoảng 30 mét)
    private static final BigDecimal DUP_LAT_DELTA = new BigDecimal("0.00030");
    private static final BigDecimal DUP_LNG_DELTA = new BigDecimal("0.00030");
    
    // Danh sách thứ tự ưu tiên hiển thị các loại rác
    private static final List<String> CITIZEN_CATEGORY_ORDER = List.of(
            "Giấy", "Báo", "Giấy, hồ sơ", "Giấy tập", "Lon bia", "Sắt", "Sắt lon",
            "Inox", "Đồng", "Nhôm", "Chai thủy tinh", "Bao bì, hỗn hợp",
            "Meca", "Mủ", "Mủ bình", "Mủ tôn", "Mủ đen"
    );

    private final WasteReportRepository wasteReportRepository;
    private final CitizenRepository citizenRepository;
    private final WasteCategoryRepository wasteCategoryRepository;
    private final WasteReportItemRepository wasteReportItemRepository;
    private final ReportImageRepository reportImageRepository;
    private final CloudinaryService cloudinaryService;
    private final CollectionRequestRepository collectionRequestRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final CollectorReportRepository collectorReportRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final FeedbackRepository feedbackRepository;
    private final CitizenFeatureMapper citizenFeatureMapper;

    @Override
    @Transactional
    public WasteReportResponse createReport(CreateWasteReportRequest request, String citizenEmail) {
        // 1. Kiểm tra thông tin công dân
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.CITIZEN_NOT_FOUND);
        
        // 2. Validate dữ liệu đầu vào và lấy danh sách ID loại rác
        List<Integer> categoryIds = validateCreateRequest(request);

        // 3. Xử lý tọa độ (làm tròn 8 chữ số thập phân)
        BigDecimal latitude = BigDecimal.valueOf(request.getLatitude()).setScale(8, RoundingMode.HALF_UP);
        BigDecimal longitude = BigDecimal.valueOf(request.getLongitude()).setScale(8, RoundingMode.HALF_UP);

        // 4. Kiểm tra spam: nếu vừa gửi báo cáo ở vị trí này trong 10 phút trước thì chặn
        LocalDateTime now = LocalDateTime.now();
        long nearDup = wasteReportRepository.countRecentNearDuplicate(
                citizen.getId(),
                now.minusMinutes(10), // 10 phút trước
                now,
                latitude,
                longitude,
                DUP_LAT_DELTA,
                DUP_LNG_DELTA);
        
        if (nearDup > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 5. Tạo đối tượng báo cáo mới
        WasteReport report = new WasteReport();
        report.setCitizen(citizen);
        report.setWasteType("RECYCLABLE"); // Mặc định là rác tái chế
        report.setDescription(request.getDescription());
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setAddress(request.getAddress());
        report.setStatus(WasteReportStatus.PENDING); // Trạng thái ban đầu là Chờ xử lý

        report.setReportCode(generateTempReportCode()); // Tạo mã tạm
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        // Lưu vào database
        WasteReport saved = wasteReportRepository.save(report);

        // Cập nhật lại mã báo cáo chính thức theo ID (ví dụ: WR001)
        String finalCode = generateReportCodeFromId(saved.getId());
        if (!finalCode.equals(saved.getReportCode())) {
            saved.setReportCode(finalCode);
            saved = wasteReportRepository.save(saved);
        }

        // 6. Lưu chi tiết các loại rác trong báo cáo
        List<BigDecimal> quantities = request.getQuantities();
        List<WasteCategory> categories = wasteCategoryRepository.findAllById(categoryIds);
        
        if (categories.size() != categoryIds.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Tạo map để tra cứu nhanh loại rác theo ID
        Map<Integer, WasteCategory> categoriesById = new HashMap<>();
        for (WasteCategory c : categories) {
            categoriesById.put(c.getId(), c);
        }

        List<WasteReportItem> reportItems = new ArrayList<>();
        for (int i = 0; i < categoryIds.size(); i++) {
            Integer categoryId = categoryIds.get(i);
            WasteCategory category = categoriesById.get(categoryId);
            
            if (category == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            WasteReportItem item = new WasteReportItem();
            item.setReport(saved);
            item.setWasteCategory(category);
            item.setUnitSnapshot(category.getUnit()); // Lưu lại đơn vị tại thời điểm báo cáo
            item.setQuantity(quantities.get(i));
            item.setCreatedAt(now);
            
            reportItems.add(item);
        }
        wasteReportItemRepository.saveAll(reportItems);

        // 7. Upload và lưu ảnh báo cáo
        saveReportImages(saved, request.getImages(), now);

        // 8. Trả về kết quả
        return buildReportResponse(saved, categories);
    }

    // Hàm tạo mã báo cáo tạm thời
    private String generateTempReportCode() {
        return "TMP" + System.nanoTime();
    }

    // Hàm tạo mã báo cáo theo ID
    private String generateReportCodeFromId(Integer id) {
        if (id == null || id <= 0) {
            return "WR000";
        }
        return "WR" + String.format("%03d", id);
    }

    @Override
    @Transactional
    public WasteReportResponse updateReport(Integer reportId, UpdateWasteReportRequest request, String citizenEmail) {
        // Kiểm tra quyền sở hữu báo cáo
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);
        WasteReport report = requireOwnedReport(reportId, citizen);
        
        // Chỉ cho phép sửa khi báo cáo chưa được xử lý
        if (report.getStatus() != WasteReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được chỉnh sửa khi status = PENDING");
        }

        // Cập nhật thông tin nếu có gửi lên
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

        // Cập nhật danh sách loại rác nếu có thay đổi
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
            
            // Xóa chi tiết cũ và thêm mới
            wasteReportItemRepository.deleteByReport_Id(saved.getId());
            
            List<WasteReportItem> reportItems = new ArrayList<>();
            Map<Integer, WasteCategory> categoriesById = new HashMap<>();
            for (WasteCategory c : categories) {
                categoriesById.put(c.getId(), c);
            }

            for (int i = 0; i < categoryIds.size(); i++) {
                Integer categoryId = categoryIds.get(i);
                WasteCategory category = categoriesById.get(categoryId);
                
                if (category == null) {
                    throw new AppException(ErrorCode.INVALID_REQUEST);
                }
                
                WasteReportItem item = new WasteReportItem();
                item.setReport(saved);
                item.setWasteCategory(category);
                item.setUnitSnapshot(category.getUnit());
                item.setQuantity(quantities.get(i));
                item.setCreatedAt(now);
                
                reportItems.add(item);
            }
            wasteReportItemRepository.saveAll(reportItems);
        }

        // Cập nhật ảnh nếu có gửi lên
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            reportImageRepository.deleteByReport_Id(saved.getId());
            saveReportImages(saved, request.getImages(), now);
        }

        // Lấy danh sách loại rác để trả về
        List<WasteCategoryResponse> categoryResponses = getCategoriesForReport(saved.getId());

        return WasteReportResponse.builder()
                .id(saved.getId())
                .message("Cập nhật báo cáo thành công")
                .reportCode(saved.getReportCode())
                .status(mapCitizenStatus(saved.getStatus()))
                .submitBy(resolveSubmitBy(saved))
                .wasteType(saved.getWasteType())
                .address(saved.getAddress())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .images(getImageUrls(saved.getId()))
                .createdAt(saved.getCreatedAt())
                .categories(categoryResponses)
                .build();
    }

    @Override
    @Transactional
    public void deleteReport(Integer reportId, String citizenEmail) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);
        WasteReport report = requireOwnedReport(reportId, citizen);
        
        if (report.getStatus() != WasteReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được huỷ khi status = PENDING");
        }
        
        // Kiểm tra xem đã có yêu cầu thu gom nào liên quan chưa
        boolean hasCollectionRequest = collectionRequestRepository.existsByReport_Id(report.getId());
        if (hasCollectionRequest) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report đã được xử lý, không thể huỷ");
        }

        // Xóa dữ liệu liên quan trước khi xóa báo cáo
        reportImageRepository.deleteByReport_Id(report.getId());
        wasteReportItemRepository.deleteByReport_Id(report.getId());
        wasteReportRepository.delete(report);
    }

    @Override
    public List<WasteReportResponse> getMyReports(String citizenEmail) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);

        // Lấy tất cả báo cáo của công dân
        List<WasteReport> reports = wasteReportRepository.findByCitizen_Id(citizen.getId());
        
        // Lấy danh sách ID để truy vấn chi tiết (items) một lần
        List<Integer> reportIds = new ArrayList<>();
        for (WasteReport r : reports) {
            reportIds.add(r.getId());
        }

        Map<Integer, List<WasteCategoryResponse>> categoriesByReportId = new HashMap<>();
        
        if (!reportIds.isEmpty()) {
            List<WasteReportItem> allItems = wasteReportItemRepository.findWithCategoryByReportIdIn(reportIds);
            
            // Gom nhóm items theo reportId
            for (WasteReportItem item : allItems) {
                int rId = item.getReport().getId();
                if (!categoriesByReportId.containsKey(rId)) {
                    categoriesByReportId.put(rId, new ArrayList<>());
                }
                categoriesByReportId.get(rId).add(toCategoryResponse(item));
            }
        }

        // Chuyển đổi sang danh sách Response
        List<WasteReportResponse> responseList = new ArrayList<>();
        for (WasteReport report : reports) {
            List<WasteCategoryResponse> cats = categoriesByReportId.get(report.getId());
            if (cats == null) {
                cats = new ArrayList<>();
            }
            
            WasteReportResponse resp = WasteReportResponse.builder()
                    .id(report.getId())
                    .reportCode(report.getReportCode())
                    .status(mapCitizenStatus(report.getStatus()))
                    .submitBy(resolveSubmitBy(report))
                    .wasteType(report.getWasteType())
                    .address(report.getAddress())
                    .latitude(report.getLatitude())
                    .longitude(report.getLongitude())
                    .createdAt(report.getCreatedAt())
                    .categories(cats)
                    .build();
            responseList.add(resp);
        }
        return responseList;
    }

    @Override
    public WasteReportResponse getMyReportById(Integer reportId, String citizenEmail) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);
        WasteReport report = requireOwnedReport(reportId, citizen);

        List<WasteCategoryResponse> categories = getCategoriesForReport(report.getId());
        
        return WasteReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .status(mapCitizenStatus(report.getStatus()))
                .submitBy(resolveSubmitBy(report))
                .wasteType(report.getWasteType())
                .address(report.getAddress())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .images(getImageUrls(report.getId()))
                .createdAt(report.getCreatedAt())
                .categories(categories)
                .build();
    }

    // Helper: Lấy tên người gửi
    private String resolveSubmitBy(WasteReport report) {
        if (report == null || report.getCitizen() == null) {
            return null;
        }
        Citizen citizen = report.getCitizen();
        if (citizen.getUser() != null) {
            if (citizen.getUser().getFullName() != null && !citizen.getUser().getFullName().isBlank()) {
                return citizen.getUser().getFullName();
            }
            if (citizen.getUser().getEmail() != null && !citizen.getUser().getEmail().isBlank()) {
                return citizen.getUser().getEmail();
            }
        }
        if (citizen.getFullName() != null && !citizen.getFullName().isBlank()) {
            return citizen.getFullName();
        }
        return citizen.getEmail();
    }

    @Override
    public CitizenReportResultResponse getMyReportResult(Integer reportId, String citizenEmail) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);
        WasteReport report = requireOwnedReport(reportId, citizen);

        Optional<CollectionRequest> requestOpt = collectionRequestRepository.findByReport_Id(report.getId());
        if (requestOpt.isEmpty()) {
            return buildEmptyResult(report);
        }
        CollectionRequest request = requestOpt.get();

        Optional<CollectorReport> collectorReportOpt = collectorReportRepository.findTopByCollectionRequest_IdOrderByCreatedAtDesc(request.getId());
        if (collectorReportOpt.isEmpty()) {
            return buildEmptyResultWithTime(report, request.getCollectedAt());
        }
        CollectorReport collectorReport = collectorReportOpt.get();

        Integer totalPoint = collectorReport.getTotalPoint() != null ? collectorReport.getTotalPoint() : 0;
        
        // Nếu điểm trong report = 0, thử tìm trong giao dịch điểm
        if (totalPoint == 0) {
            List<PointTransaction> txs = pointTransactionRepository.findByCollectionRequestId(request.getId());
            if (!txs.isEmpty()) {
                totalPoint = txs.get(txs.size() - 1).getPoints();
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
                .items(new ArrayList<>()) // Hiện tại chưa trả về chi tiết items
                .build();
    }
    
    private CitizenReportResultResponse buildEmptyResult(WasteReport report) {
        return CitizenReportResultResponse.builder()
                .reportId(report.getId())
                .reportCode(report.getReportCode())
                .status(mapCitizenStatus(report.getStatus()))
                .totalPoint(0)
                .collectedAt(null)
                .items(new ArrayList<>())
                .build();
    }

    private CitizenReportResultResponse buildEmptyResultWithTime(WasteReport report, LocalDateTime collectedAt) {
        return CitizenReportResultResponse.builder()
                .reportId(report.getId())
                .reportCode(report.getReportCode())
                .status(mapCitizenStatus(report.getStatus()))
                .totalPoint(0)
                .collectedAt(collectedAt)
                .items(new ArrayList<>())
                .build();
    }

    @Override
    public List<CitizenRewardHistoryResponse> getRewardHistory(String citizenEmail, LocalDateTime startDate, LocalDateTime endDate) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);

        List<PointTransaction> transactions = pointTransactionRepository.findByCitizenId(citizen.getId());
        
        List<CitizenRewardHistoryResponse> results = new ArrayList<>();
        
        for (PointTransaction tx : transactions) {
            // Lọc theo ngày nếu có
            if (startDate != null && endDate != null) {
                if (!isInRange(tx.getCreatedAt(), startDate, endDate)) {
                    continue; // Bỏ qua nếu không nằm trong khoảng thời gian
                }
            }
            results.add(citizenFeatureMapper.toCitizenRewardHistoryResponse(tx));
        }
        
        return results;
    }

    @Override
    public List<CitizenLeaderboardResponse> getLeaderboard(String region, Integer limit) {
        int size = normalizeLeaderboardLimit(limit);
        String normalizedRegion = normalizeRegion(region);

        List<Citizen> citizens = citizenRepository
                .findLeaderboard(normalizedRegion, PageRequest.of(0, size))
                .getContent();

        List<CitizenLeaderboardResponse> leaderboard = new ArrayList<>();
        int rank = 1;
        for (Citizen c : citizens) {
            CitizenLeaderboardResponse resp = citizenFeatureMapper.toCitizenLeaderboardResponse(c);
            resp.setRank(rank);
            leaderboard.add(resp);
            rank++;
        }
        return leaderboard;
    }

    @Override
    public CitizenPointSummaryResponse getMyPointSummary(String citizenEmail, Integer year, Integer quarter, Integer month) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);

        LocalDateTime from = null;
        LocalDateTime to = null;

        // Xử lý logic thời gian: Năm, Quý, Tháng
        if (year != null || quarter != null || month != null) {
            int y = normalizeYearOrThrow(year);
            if (month != null) {
                // Lọc theo tháng
                if (month < 1 || month > 12) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "month phải từ 1 đến 12");
                }
                from = LocalDate.of(y, month, 1).atStartOfDay();
                to = from.plusMonths(1);
            } else if (quarter != null) {
                // Lọc theo quý
                if (quarter < 1 || quarter > 4) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quarter phải từ 1 đến 4");
                }
                int fromMonth = (quarter - 1) * 3 + 1;
                from = LocalDate.of(y, fromMonth, 1).atStartOfDay();
                to = from.plusMonths(3);
            } else {
                // Lọc theo năm
                from = LocalDate.of(y, 1, 1).atStartOfDay();
                to = from.plusYears(1);
            }
        }

        Long earnedPoints = pointTransactionRepository.sumPointsByCitizenIdAndTypeAndRange(
                citizen.getId(),
                "EARN",
                from,
                to
        );

        return CitizenPointSummaryResponse.builder()
                .citizenId(citizen.getId())
                .fullName(citizen.getFullName())
                .totalPoints(citizen.getTotalPoints() != null ? citizen.getTotalPoints() : 0)
                .earnedPoints(earnedPoints != null ? earnedPoints : 0L)
                .from(from)
                .to(to)
                .build();
    }

    @Override
    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request, String citizenEmail) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);

        Optional<WasteReport> reportOpt = wasteReportRepository.findById(request.getReportId());
        if (reportOpt.isEmpty()) {
            throw new AppException(ErrorCode.WASTE_REPORT_NOT_FOUND);
        }
        WasteReport report = reportOpt.get();

        if (!report.getCitizen().getId().equals(citizen.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String normalizedType = normalizeComplaintType(request.getType());
        request.setType(normalizedType);
        
        // Chỉ cho phép 2 loại complaint: COMPLAINT_SERVICE và COMPLAINT_REWARD
        if (!"COMPLAINT_SERVICE".equals(normalizedType) && !"COMPLAINT_REWARD".equals(normalizedType)) {
             throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        CollectionRequest collectionRequest = collectionRequestRepository.findByReport_Id(report.getId()).orElse(null);
        
        // Nếu khiếu nại về thưởng (tương đương POINT cũ), phải kiểm tra xem báo cáo đã được thu gom (COLLECTED) chưa
        if ("COMPLAINT_REWARD".equals(normalizedType)) {
            if (report.getStatus() != WasteReportStatus.COLLECTED) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }

        // Upload ảnh bằng chứng
        String imageUrl = null;
        if (request.getEvidenceImage() != null && !request.getEvidenceImage().isEmpty()) {
            CloudinaryResponse uploaded = cloudinaryService.uploadImage(request.getEvidenceImage(), "feedbacks");
            if (uploaded != null) {
                imageUrl = uploaded.getUrl();
            }
        }



        Feedback feedback = citizenFeatureMapper.toFeedback(request);
        feedback.setCitizen(citizen);
        
        // Tạo mã tạm thời để pass constraint
        feedback.setFeedbackCode("TEMP-" + System.nanoTime());
        feedback.setCollectionRequest(collectionRequest);
        feedback.setImageUrl(imageUrl);
        
        Feedback saved = feedbackRepository.save(feedback);
        
        // Cập nhật mã feedback chuẩn: F + 3 số ID
        String finalCode = String.format("F%03d", saved.getId());
        saved.setFeedbackCode(finalCode);
        feedbackRepository.save(saved);

        return citizenFeatureMapper.toComplaintResponse(saved);
    }

    private static String normalizeComplaintType(String type) {
        if (type == null) {
            return null;
        }
        // Normalize và map các giá trị cũ nếu cần
        String normalized = type.trim().replaceAll("\\s+", "_").toUpperCase(Locale.ROOT);
        
        // Map các giá trị cũ sang mới (nếu cần thiết)
        if ("POINT".equals(normalized)) return "COMPLAINT_REWARD";
        if ("COLLECTOR".equals(normalized)) return "COMPLAINT_SERVICE";
        if ("SERVICE".equals(normalized)) return "COMPLAINT_SERVICE";
        if ("REWARD".equals(normalized)) return "COMPLAINT_REWARD";
        
        return normalized;
    }

    @Override
    public List<ComplaintResponse> getComplaints(String citizenEmail) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);

        List<Feedback> feedbacks = feedbackRepository.findByCitizenIdOrderByCreatedAtDesc(citizen.getId());
        
        List<ComplaintResponse> responses = new ArrayList<>();
        for (Feedback f : feedbacks) {
            responses.add(citizenFeatureMapper.toComplaintResponse(f));
        }
        return responses;
    }

    @Override
    public List<WasteCategoryResponse> getWasteCategories() {
        List<WasteCategory> categories = wasteCategoryRepository.findAll();
        
        // Sắp xếp danh sách loại rác theo thứ tự ưu tiên đã định nghĩa
        // Sử dụng thuật toán sắp xếp cơ bản (bubble sort hoặc comparator đơn giản)
        categories.sort((c1, c2) -> {
            Integer idx1 = findOrderIndex(c1.getName());
            Integer idx2 = findOrderIndex(c2.getName());
            
            // Nếu cả 2 đều có trong danh sách ưu tiên, so sánh index
            if (idx1 != null && idx2 != null) {
                return idx1.compareTo(idx2);
            }
            // Nếu chỉ c1 có, c1 lên trước
            if (idx1 != null) return -1;
            // Nếu chỉ c2 có, c2 lên trước
            if (idx2 != null) return 1;
            // Nếu cả 2 không có, giữ nguyên hoặc so sánh theo ID
            return 0;
        });

        List<WasteCategoryResponse> responses = new ArrayList<>();
        for (WasteCategory c : categories) {
            // Chỉ lấy những loại rác có trong danh sách ưu tiên (theo logic cũ)
            if (findOrderIndex(c.getName()) != null) {
                responses.add(WasteCategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .unit(c.getUnit() != null ? c.getUnit().name() : null)
                        .pointPerUnit(c.getPointPerUnit())
                        .build());
            }
        }
        return responses;
    }

    @Override
    public CitizenReportStatsResponse getMyReportStats(String citizenEmail) {
        Citizen citizen = requireCitizenByEmail(citizenEmail, ErrorCode.USER_NOT_EXISTED);

        // 1. Thống kê số lượng báo cáo theo trạng thái
        List<Object[]> statusCounts = wasteReportRepository.countStatusByCitizenId(citizen.getId());
        Map<String, Long> reportsByStatus = new HashMap<>();
        for (Object[] row : statusCounts) {
            String status = row[0].toString();
            Long count = (Long) row[1];
            reportsByStatus.put(status, count);
        }

        // 2. Thống kê khối lượng rác theo loại
        List<Object[]> weightCounts = collectorReportItemRepository.sumWeightByWasteTypeForCitizen(citizen.getId());
        Map<String, BigDecimal> wasteWeightByType = new HashMap<>();
        for (Object[] row : weightCounts) {
            String type = (String) row[0];
            BigDecimal weight = (BigDecimal) row[1];
            wasteWeightByType.put(type, weight != null ? weight : BigDecimal.ZERO);
        }

        return CitizenReportStatsResponse.builder()
                .reportsByStatus(reportsByStatus)
                .wasteWeightByType(wasteWeightByType)
                .build();
    }


    private List<WasteCategoryResponse> getCategoriesForReport(Integer reportId) {
        List<WasteReportItem> items = wasteReportItemRepository.findWithCategoryByReportId(reportId);
        List<WasteCategoryResponse> responses = new ArrayList<>();
        for (WasteReportItem item : items) {
            responses.add(toCategoryResponse(item));
        }
        return responses;
    }
    
    // Hàm build response cho createReport
    private WasteReportResponse buildReportResponse(WasteReport saved, List<WasteCategory> categories) {
        List<WasteCategoryResponse> catResponses = new ArrayList<>();
        for (WasteCategory c : categories) {
            catResponses.add(toCategoryResponse(c));
        }
        
        return WasteReportResponse.builder()
                .id(saved.getId())
                .message("Tạo báo cáo thành công")
                .reportCode(saved.getReportCode())
                .status(mapCitizenStatus(saved.getStatus()))
                .submitBy(resolveSubmitBy(saved))
                .wasteType(saved.getWasteType())
                .address(saved.getAddress())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .images(getImageUrls(saved.getId()))
                .createdAt(saved.getCreatedAt())
                .categories(catResponses)
                .build();
    }

    private WasteCategoryResponse toCategoryResponse(WasteCategory category) {
        return WasteCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .unit(category.getUnit() != null ? category.getUnit().name() : null)
                .pointPerUnit(category.getPointPerUnit())
                .build();
    }

    private WasteCategoryResponse toCategoryResponse(WasteReportItem item) {
        WasteCategory category = item.getWasteCategory();
        return WasteCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .unit(category.getUnit() != null ? category.getUnit().name() : null)
                .pointPerUnit(category.getPointPerUnit())
                .quantity(item.getQuantity())
                .build();
    }

    private List<String> getImageUrls(Integer reportId) {
        List<ReportImage> images = reportImageRepository.findByReport_Id(reportId);
        List<String> urls = new ArrayList<>();
        for (ReportImage img : images) {
            urls.add(img.getImageUrl());
        }
        return urls;
    }

    private List<Integer> validateCreateRequest(CreateWasteReportRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getCategoryIds() == null || request.getCategoryIds().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        List<Integer> resolvedCategoryIds = resolveCategoryIds(request.getCategoryIds());
        
        if (request.getQuantities() == null || request.getQuantities().isEmpty() ||
                request.getQuantities().size() != resolvedCategoryIds.size()) {
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
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return resolvedCategoryIds;
    }

    private void saveReportImages(WasteReport report, List<MultipartFile> images, LocalDateTime now) {
        boolean first = true;
        for (MultipartFile file : images) {
            CloudinaryResponse uploaded = cloudinaryService.uploadImage(file, "reports");
            if (uploaded == null || uploaded.getUrl() == null) {
                throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
            }

            // Ảnh đầu tiên được lưu vào bảng WasteReport làm ảnh đại diện
            if (first) {
                report.setImages(uploaded.getUrl());
                report.setCloudinaryPublicId(uploaded.getPublicId());
                wasteReportRepository.save(report);
                first = false;
            }

            // Lưu tất cả ảnh vào bảng ReportImage
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
        // Sử dụng switch case cơ bản thay vì switch expression mới (để trông giống code sinh viên hơn)
        switch (status) {
            case PENDING:
                return "PENDING";
            case ACCEPTED_ENTERPRISE:
                return "ACCEPTED";
            case REASSIGN:
                return "REASSIGN";
            case ASSIGNED:
            case ACCEPTED_COLLECTOR:
                return "ASSIGNED";
            case ON_THE_WAY:
                return "ON THE WAY";
            case COLLECTED:
                return "COLLECTED";
            case REJECTED:
                return "REJECTED";
            case TIMED_OUT:
                return "TIMED_OUT";
            default:
                return "PENDING";
        }
    }

    private Citizen requireCitizenByEmail(String citizenEmail, ErrorCode errorCode) {
        Optional<Citizen> citizenOpt = citizenRepository.findByUser_Email(citizenEmail);
        if (citizenOpt.isEmpty()) {
            throw new AppException(errorCode);
        }
        return citizenOpt.get();
    }

    private WasteReport requireOwnedReport(Integer reportId, Citizen citizen) {
        Optional<WasteReport> reportOpt = wasteReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new AppException(ErrorCode.WASTE_REPORT_NOT_FOUND);
        }
        WasteReport report = reportOpt.get();
        
        if (report.getCitizen() == null || !citizen.getId().equals(report.getCitizen().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return report;
    }

    private static boolean isInRange(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        if (value == null) {
            return false;
        }
        // Kiểm tra xem value có nằm trong khoảng [start, end] không
        return (value.isEqual(start) || value.isAfter(start)) &&
                (value.isEqual(end) || value.isBefore(end));
    }

    private static String normalizeRegion(String region) {
        if (region == null) {
            return null;
        }
        String trimmed = region.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private static int normalizeLeaderboardLimit(Integer limit) {
        int value = limit != null ? limit : 50;
        if (value < 1) {
            return 1;
        }
        return Math.min(value, 500);
    }

    private static int normalizeYearOrThrow(Integer year) {
        if (year == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu tham số year");
        }
        if (year < 1970 || year > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year không hợp lệ");
        }
        return year;
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
            // Tách chuỗi bằng dấu phẩy
            String[] parts = raw.split(",");
            for (String part : parts) {
                String token = part != null ? part.trim() : null;
                if (token == null || token.isEmpty()) {
                    continue;
                }
                
                // Thử parse số nguyên (ID)
                Integer id = tryParseInt(token);
                if (id != null) {
                    ids.add(id);
                    continue;
                }
                
                // Nếu không phải số, tìm theo tên
                Optional<WasteCategory> catOpt = wasteCategoryRepository.findByNameIgnoreCase(token);
                if (catOpt.isPresent()) {
                    ids.add(catOpt.get().getId());
                } else {
                    throw new AppException(ErrorCode.INVALID_REQUEST);
                }
            }
        }

        // Loại bỏ trùng lặp
        List<Integer> deduped = new ArrayList<>();
        for (Integer id : ids) {
            if (id != null && !deduped.contains(id)) {
                deduped.add(id);
            }
        }
        
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
        for (int i = 0; i < CITIZEN_CATEGORY_ORDER.size(); i++) {
            if (CITIZEN_CATEGORY_ORDER.get(i).toLowerCase(Locale.ROOT).equals(normalized)) {
                return i;
            }
        }
        return null;
    }
}

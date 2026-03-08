package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseCitizenPointSummaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseMonthlyWasteVolumeResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseQuarterlyWasteVolumeResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteVolumeStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseGeneralStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnterpriseStatisticsServiceImpl implements EnterpriseStatisticsService {

    private final CollectionRequestRepository collectionRequestRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    public EnterpriseGeneralStatsResponse getGeneralStats(Integer enterpriseId) {
        validateEnterprise(enterpriseId);

        // 1. Reports by status
        List<Object[]> statusCounts = collectionRequestRepository.countStatusByEnterpriseId(enterpriseId);
        Map<String, Long> reportsByStatus = new HashMap<>();
        for (Object[] row : statusCounts) {
            String status = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = (Long) row[1];
            reportsByStatus.put(status, count);
        }

        // 2. Waste weight by type
        List<Object[]> weightCounts = collectorReportItemRepository.sumWeightByWasteTypeForEnterprise(enterpriseId);
        Map<String, BigDecimal> wasteWeightByType = new HashMap<>();
        for (Object[] row : weightCounts) {
            String type = (String) row[0];
            BigDecimal weight = (BigDecimal) row[1];
            wasteWeightByType.put(type, weight != null ? weight : BigDecimal.ZERO);
        }

        // 3. Collector performance
        List<Object[]> collectorPerfRows = collectionRequestRepository.getCollectorPerformanceForEnterprise(enterpriseId);
        List<EnterpriseGeneralStatsResponse.CollectorPerformanceSummary> collectorPerformance = new ArrayList<>();
        for (Object[] row : collectorPerfRows) {
            collectorPerformance.add(EnterpriseGeneralStatsResponse.CollectorPerformanceSummary.builder()
                    .collectorId((Integer) row[0])
                    .fullName((String) row[1])
                    .requestsCompleted((Long) row[2])
                    .totalWeight((BigDecimal) row[3])
                    .build());
        }

        // 4. Point stats
        Long totalPoints = pointTransactionRepository.sumTotalPointsDistributedByEnterprise(enterpriseId);
        List<Object[]> pointsPerMonthRows = pointTransactionRepository.sumPointsDistributedByEnterprisePerMonth(enterpriseId);
        Map<String, Long> pointsPerMonthMap = new HashMap<>();
        for (Object[] row : pointsPerMonthRows) {
            Integer y = (Integer) row[0];
            Integer m = (Integer) row[1];
            Long points = (Long) row[2];
            String key = String.format("%d-%02d", y, m);
            pointsPerMonthMap.put(key, points);
        }

        EnterpriseGeneralStatsResponse.PointDistributionStats pointStats = EnterpriseGeneralStatsResponse.PointDistributionStats.builder()
                .totalPointsDistributed(totalPoints != null ? totalPoints : 0L)
                .pointsDistributedPerMonth(pointsPerMonthMap)
                .build();

        return EnterpriseGeneralStatsResponse.builder()
                .reportsByStatus(reportsByStatus)
                .wasteWeightByType(wasteWeightByType)
                .collectorPerformance(collectorPerformance)
                .pointStats(pointStats)
                .build();
    }

    @Override
    public EnterpriseWasteVolumeStatsResponse getWasteVolumeStats(Integer enterpriseId, Integer year) {
        validateEnterprise(enterpriseId);
        int y = normalizeYear(year);

        LocalDateTime from = LocalDate.of(y, 1, 1).atStartOfDay();
        LocalDateTime to = from.plusYears(1);

        // Lấy dữ liệu thống kê từ DB
        List<CollectionRequestRepository.EnterpriseMonthlyWasteVolumeView> byMonthViews =
                collectionRequestRepository.sumCompletedWeightByMonthForEnterprise(enterpriseId, from, to);
        List<CollectionRequestRepository.EnterpriseQuarterlyWasteVolumeView> byQuarterViews =
                collectionRequestRepository.sumCompletedWeightByQuarterForEnterprise(enterpriseId, from, to);

        // Chuyển đổi dữ liệu tháng
        List<EnterpriseMonthlyWasteVolumeResponse> byMonth = new ArrayList<>();
        BigDecimal totalWeightKg = BigDecimal.ZERO;
        long totalRequests = 0L;

        for (CollectionRequestRepository.EnterpriseMonthlyWasteVolumeView view : byMonthViews) {
            EnterpriseMonthlyWasteVolumeResponse response = new EnterpriseMonthlyWasteVolumeResponse();
            response.setYear(view.getYearValue());
            response.setMonth(view.getMonthValue());
            response.setTotalWeightKg(nullToZero(view.getTotalWeightKg()));
            response.setTotalRequests(view.getTotalRequests() != null ? view.getTotalRequests() : 0L);
            byMonth.add(response);

            // Cộng dồn tổng cả năm
            totalWeightKg = totalWeightKg.add(response.getTotalWeightKg());
            totalRequests += response.getTotalRequests();
        }

        // Chuyển đổi dữ liệu quý
        List<EnterpriseQuarterlyWasteVolumeResponse> byQuarter = new ArrayList<>();
        for (CollectionRequestRepository.EnterpriseQuarterlyWasteVolumeView view : byQuarterViews) {
            EnterpriseQuarterlyWasteVolumeResponse response = new EnterpriseQuarterlyWasteVolumeResponse();
            response.setYear(view.getYearValue());
            response.setQuarter(view.getQuarterValue());
            response.setTotalWeightKg(nullToZero(view.getTotalWeightKg()));
            response.setTotalRequests(view.getTotalRequests() != null ? view.getTotalRequests() : 0L);
            byQuarter.add(response);
        }

        // Tạo kết quả trả về
        return EnterpriseWasteVolumeStatsResponse.builder()
                .year(y)
                .totalWeightKg(totalWeightKg)
                .totalRequests(totalRequests)
                .byMonth(byMonth)
                .byQuarter(byQuarter)
                .build();
    }

    @Override
    public List<EnterpriseCitizenPointSummaryResponse> getCitizenPointSummaries(Integer enterpriseId, Integer year, Integer quarter, Integer month) {
        validateEnterprise(enterpriseId);
        int y = normalizeYear(year);

        LocalDateTime from;
        LocalDateTime to;

        // Xác định khoảng thời gian (tháng, quý hoặc cả năm)
        if (month != null) {
            int m = month;
            if (m < 1 || m > 12) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "month phải từ 1 đến 12");
            }
            from = LocalDate.of(y, m, 1).atStartOfDay();
            to = from.plusMonths(1);
        } else if (quarter != null) {
            int q = quarter;
            if (q < 1 || q > 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quarter phải từ 1 đến 4");
            }
            int fromMonth = (q - 1) * 3 + 1;
            from = LocalDate.of(y, fromMonth, 1).atStartOfDay();
            to = from.plusMonths(3);
        } else {
            from = LocalDate.of(y, 1, 1).atStartOfDay();
            to = from.plusYears(1);
        }

        // Lấy dữ liệu từ DB
        var views = collectionRequestRepository.summarizeCitizenPointsForEnterprise(enterpriseId, from, to);
        
        // Chuyển đổi dữ liệu sang DTO trả về
        List<EnterpriseCitizenPointSummaryResponse> result = new ArrayList<>();
        for (var v : views) {
            EnterpriseCitizenPointSummaryResponse response = new EnterpriseCitizenPointSummaryResponse();
            response.setCitizenId(v.getCitizenId());
            response.setFullName(v.getFullName());
            response.setPhone(v.getPhone());
            response.setTotalPoints(v.getTotalPoints() != null ? v.getTotalPoints() : 0L);
            response.setTotalWeightKg(nullToZero(v.getTotalWeightKg()));
            response.setTotalCollections(v.getTotalCollections() != null ? v.getTotalCollections() : 0L);
            
            result.add(response);
        }

        return result;
    }

    private Enterprise validateEnterprise(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));
    }

    private int normalizeYear(Integer year) {
        if (year == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu tham số year");
        }
        if (year < 1970 || year > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year không hợp lệ");
        }
        return year;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightChartResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightDailyChartResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminDailyCollectedWeightResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminMonthlyCollectedWeightResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminSystemAnalyticsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminWasteCategoryCollectedWeightResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final WasteReportRepository wasteReportRepository;
    private final CitizenRepository citizenRepository;
    private final CollectorRepository collectorRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    public AdminSystemAnalyticsResponse getSystemAnalytics() {
        // 1. Report Stats
        long pending = collectionRequestRepository.countByStatus(CollectionRequestStatus.PENDING);
        long accepted = collectionRequestRepository.countByStatus(CollectionRequestStatus.ACCEPTED_ENTERPRISE);
        long rejected = collectionRequestRepository.countByStatus(CollectionRequestStatus.REJECTED);
        long assigned = collectionRequestRepository.countByStatus(CollectionRequestStatus.ASSIGNED) +
                        collectionRequestRepository.countByStatus(CollectionRequestStatus.ACCEPTED_COLLECTOR) +
                        collectionRequestRepository.countByStatus(CollectionRequestStatus.ON_THE_WAY) +
                        collectionRequestRepository.countByStatus(CollectionRequestStatus.REASSIGN);
        long collected = collectionRequestRepository.countByStatus(CollectionRequestStatus.COLLECTED);
        long completed = collectionRequestRepository.countByStatus(CollectionRequestStatus.COMPLETED);
        long totalRequests = collectionRequestRepository.count();

        AdminSystemAnalyticsResponse.ReportStats reportStats = AdminSystemAnalyticsResponse.ReportStats.builder()
                .totalRequests(totalRequests)
                .pending(pending)
                .accepted(accepted)
                .rejected(rejected)
                .assigned(assigned)
                .collected(collected)
                .completed(completed)
                .build();

        // 2. Waste Type Stats
        List<AdminSystemAnalyticsResponse.WasteTypeStats> wasteTypeStats = collectorReportItemRepository.sumGlobalCollectedWeightByCategory()
                .stream()
                .map(view -> new AdminSystemAnalyticsResponse.WasteTypeStats(view.getCategoryName(), view.getTotalWeightKg()))
                .toList();

        // 3. Citizen Activity Stats
        long totalCitizens = citizenRepository.count();
        long totalReportsCreated = wasteReportRepository.count();
        Long totalPointsEarned = pointTransactionRepository.sumTotalPointsDistributed();
        if (totalPointsEarned == null) totalPointsEarned = 0L;

        AdminSystemAnalyticsResponse.CitizenActivityStats citizenActivityStats = AdminSystemAnalyticsResponse.CitizenActivityStats.builder()
                .totalCitizens(totalCitizens)
                .totalReportsCreated(totalReportsCreated)
                .totalPointsEarned(totalPointsEarned)
                .build();

        // 4. Collector Activity Stats
        long totalCollectors = collectorRepository.count();
        long totalRequestsCompleted = completed; // Reuse completed count
        BigDecimal totalWasteCollected = collectionRequestRepository.sumTotalActualWeight();
        if (totalWasteCollected == null) totalWasteCollected = BigDecimal.ZERO;

        AdminSystemAnalyticsResponse.CollectorActivityStats collectorActivityStats = AdminSystemAnalyticsResponse.CollectorActivityStats.builder()
                .totalCollectors(totalCollectors)
                .totalRequestsCompleted(totalRequestsCompleted)
                .totalWasteCollected(totalWasteCollected)
                .build();

        // 5. Collector Performance Stats
        List<Object[]> performanceData = collectionRequestRepository.getGlobalCollectorPerformance();
        List<AdminSystemAnalyticsResponse.CollectorPerformanceStats> collectorPerformanceStats = performanceData.stream()
                .map(row -> new AdminSystemAnalyticsResponse.CollectorPerformanceStats(
                        (Integer) row[0],
                        (String) row[1],
                        (Long) row[2],
                        (BigDecimal) row[3]
                ))
                .toList();

        // 6. Reward Stats
        Long totalPointsDistributed = totalPointsEarned; // Reuse
        double averagePointsPerCitizen = totalCitizens > 0 ? (double) totalPointsDistributed / totalCitizens : 0;
        List<Object[]> monthlyPointsData = pointTransactionRepository.sumPointsDistributedPerMonth();
        List<AdminSystemAnalyticsResponse.MonthlyPointStats> monthlyPointStats = monthlyPointsData.stream()
                .map(row -> new AdminSystemAnalyticsResponse.MonthlyPointStats(
                        (Integer) row[0],
                        (Integer) row[1],
                        (Long) row[2]
                ))
                .toList();

        // Get Top Citizens by Points
        List<AdminSystemAnalyticsResponse.CitizenPointStats> topCitizens = citizenRepository.findLeaderboard(null, PageRequest.of(0, 10))
                .getContent()
                .stream()
                .map(citizen -> new AdminSystemAnalyticsResponse.CitizenPointStats(
                        citizen.getId(),
                        citizen.getFullName(),
                        citizen.getTotalPoints() != null ? citizen.getTotalPoints() : 0L
                ))
                .toList();

        AdminSystemAnalyticsResponse.RewardStats rewardStats = AdminSystemAnalyticsResponse.RewardStats.builder()
                .totalPointsDistributed(totalPointsDistributed)
                .averagePointsPerCitizen(averagePointsPerCitizen)
                .pointsDistributedPerMonth(monthlyPointStats)
                .topCitizens(topCitizens)
                .build();

        return AdminSystemAnalyticsResponse.builder()
                .reportStats(reportStats)
                .wasteTypeStats(wasteTypeStats)
                .citizenActivityStats(citizenActivityStats)
                .collectorActivityStats(collectorActivityStats)
                .collectorPerformanceStats(collectorPerformanceStats)
                .rewardStats(rewardStats)
                .build();
    }

    @Override
    public AdminCollectedWeightChartResponse getCollectedWeightChart(Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();

        BigDecimal totalWeightKg = collectionRequestRepository.sumActualWeightForYear(targetYear);
        if (totalWeightKg == null) {
            totalWeightKg = BigDecimal.ZERO;
        }

        List<CollectionRequestRepository.AdminMonthlyCollectedWeightView> monthlyViews =
                collectionRequestRepository.sumActualWeightByMonthForYear(targetYear);

        Map<Integer, BigDecimal> monthlyByMonth = new HashMap<>();
        for (CollectionRequestRepository.AdminMonthlyCollectedWeightView view : monthlyViews) {
            monthlyByMonth.put(view.getMonth(), safeWeight(view.getTotalWeightKg()));
        }

        List<AdminMonthlyCollectedWeightResponse> monthly = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> AdminMonthlyCollectedWeightResponse.builder()
                        .month(month)
                        .totalWeightKg(monthlyByMonth.getOrDefault(month, BigDecimal.ZERO))
                        .build())
                .toList();

        List<AdminWasteCategoryCollectedWeightResponse> byCategory = collectorReportItemRepository
                .sumCollectedWeightByCategoryForYear(targetYear)
                .stream()
                .map(view -> AdminWasteCategoryCollectedWeightResponse.builder()
                        .categoryId(view.getCategoryId())
                        .categoryName(view.getCategoryName())
                        .totalWeightKg(safeWeight(view.getTotalWeightKg()))
                        .build())
                .toList();

        return AdminCollectedWeightChartResponse.builder()
                .year(targetYear)
                .totalWeightKg(totalWeightKg)
                .monthly(monthly)
                .byCategory(byCategory)
                .build();
    }

    @Override
    public AdminCollectedWeightDailyChartResponse getCollectedWeightDailyChart(Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        YearMonth targetYearMonth = YearMonth.of(targetYear, targetMonth);

        BigDecimal totalWeightKg = collectionRequestRepository.sumActualWeightForMonth(targetYear, targetMonth);
        if (totalWeightKg == null) {
            totalWeightKg = BigDecimal.ZERO;
        }

        List<CollectionRequestRepository.AdminDailyCollectedWeightView> dailyViews =
                collectionRequestRepository.sumActualWeightByDayForMonth(targetYear, targetMonth);

        Map<Integer, BigDecimal> dailyByDay = new HashMap<>();
        for (CollectionRequestRepository.AdminDailyCollectedWeightView view : dailyViews) {
            dailyByDay.put(view.getDay(), safeWeight(view.getTotalWeightKg()));
        }

        List<AdminDailyCollectedWeightResponse> daily = IntStream.rangeClosed(1, targetYearMonth.lengthOfMonth())
                .mapToObj(day -> AdminDailyCollectedWeightResponse.builder()
                        .day(day)
                        .totalWeightKg(dailyByDay.getOrDefault(day, BigDecimal.ZERO))
                        .build())
                .toList();

        return AdminCollectedWeightDailyChartResponse.builder()
                .year(targetYear)
                .month(targetMonth)
                .totalWeightKg(totalWeightKg)
                .daily(daily)
                .build();
    }

    private BigDecimal safeWeight(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

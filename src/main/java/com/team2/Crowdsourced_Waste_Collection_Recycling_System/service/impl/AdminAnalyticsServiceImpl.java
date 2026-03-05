package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightChartResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightDailyChartResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminDailyCollectedWeightResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminMonthlyCollectedWeightResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminWasteCategoryCollectedWeightResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
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

package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.PointRuleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.feedback.FeedbackRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@ConditionalOnProperty(name = "app.seed.modular", havingValue = "true")
public class SeedWorkflowInitializer {

    @Bean
    public CommandLineRunner seedWorkflow(
            CitizenRepository citizenRepository,
            WasteReportRepository wasteReportRepository,
            ReportImageRepository reportImageRepository,
            CollectionRequestRepository collectionRequestRepository,
            CollectionTrackingRepository collectionTrackingRepository,
            CollectorReportRepository collectorReportRepository,
            CollectorReportImageRepository collectorReportImageRepository,
            PointRuleRepository pointRuleRepository,
            FeedbackRepository feedbackRepository
    ) {
        return args -> {
            Citizen citizen = citizenRepository.findAll().stream().findFirst().orElse(null);
            if (citizen == null) return;

            LocalDateTime now = LocalDateTime.now();

            WasteReport r3 = createWasteReportIfNotFound(wasteReportRepository, "WR-MOD-003", citizen,
                    WasteReportStatus.PENDING, now.minusHours(4));
            createReportImageIfMissing(reportImageRepository, r3, "https://example.com/reports/mod-003.jpg");
            CollectionRequest cr3 = ensureCollectionRequest(collectionRequestRepository, "CR-MOD-003", r3, null,
                    null, CollectionRequestStatus.ASSIGNED,
                    now.minusHours(2), null, null, null, null, now.minusHours(4), now.minusHours(2));
            ensureTrackingIfMissing(collectionTrackingRepository, cr3, null, "assigned", now.minusHours(2));

            WasteReport r4 = createWasteReportIfNotFound(wasteReportRepository, "WR-MOD-004", citizen,
                    WasteReportStatus.PENDING, now.minusHours(5));
            createReportImageIfMissing(reportImageRepository, r4, "https://example.com/reports/mod-004.jpg");
            CollectionRequest cr4 = ensureCollectionRequest(collectionRequestRepository, "CR-MOD-004", r4, null,
                    null, CollectionRequestStatus.ON_THE_WAY,
                    now.minusHours(4), now.minusHours(3), now.minusHours(2), null, null, now.minusHours(5),
                    now.minusHours(2));
            ensureTrackingIfMissing(collectionTrackingRepository, cr4, null, "assigned", now.minusHours(4));
            ensureTrackingIfMissing(collectionTrackingRepository, cr4, null, "accepted", now.minusHours(3));
            ensureTrackingIfMissing(collectionTrackingRepository, cr4, null, "started", now.minusHours(2));

            WasteReport r5 = createWasteReportIfNotFound(wasteReportRepository, "WR-MOD-005", citizen,
                    WasteReportStatus.COLLECTED, now.minusDays(1));
            createReportImageIfMissing(reportImageRepository, r5, "https://example.com/reports/mod-005.jpg");
            CollectionRequest cr5 = ensureCollectionRequest(collectionRequestRepository, "CR-MOD-005", r5, null,
                    null, CollectionRequestStatus.COLLECTED,
                    now.minusDays(1).plusHours(1), now.minusDays(1).plusHours(2), now.minusDays(1).plusHours(3),
                    new BigDecimal("8.50"),
                    now.minusDays(1).plusHours(5), now.minusDays(1), now.minusDays(1).plusHours(5));
            ensureTrackingIfMissing(collectionTrackingRepository, cr5, null, "assigned",
                    now.minusDays(1).plusHours(1));
            ensureTrackingIfMissing(collectionTrackingRepository, cr5, null, "accepted",
                    now.minusDays(1).plusHours(2));
            ensureTrackingIfMissing(collectionTrackingRepository, cr5, null, "started",
                    now.minusDays(1).plusHours(3));
            ensureTrackingIfMissing(collectionTrackingRepository, cr5, null, "collected",
                    now.minusDays(1).plusHours(5));

            ensureCollectorReportIfMissing(collectorReportRepository, collectorReportImageRepository, cr5,
                    now.minusDays(1).plusHours(5));

            ensureFeedback(feedbackRepository, citizen, cr5, now.minusDays(1).plusHours(6));
        };
    }

    private WasteReport createWasteReportIfNotFound(WasteReportRepository repo, String reportCode, Citizen citizen,
                                                    WasteReportStatus status, LocalDateTime createdAt) {
        return repo.findByReportCode(reportCode).orElseGet(() -> {
            WasteReport report = new WasteReport();
            report.setReportCode(reportCode);
            report.setCitizen(citizen);
            report.setWasteType("RECYCLABLE");
            report.setDescription("Modular seed " + reportCode);
            report.setLatitude(new BigDecimal("10.77653000"));
            report.setLongitude(new BigDecimal("106.70098000"));
            report.setAddress("Seed address");
            report.setImages("https://example.com/reports/" + reportCode + ".jpg");
            report.setStatus(status);
            report.setCreatedAt(createdAt);
            report.setUpdatedAt(createdAt);
            return repo.save(report);
        });
    }

    private void createReportImageIfMissing(ReportImageRepository repo, WasteReport report, String imageUrl) {
        if (report == null || report.getId() == null) return;
        if (!repo.findByReport_Id(report.getId()).isEmpty()) return;
        ReportImage image = new ReportImage();
        image.setReport(report);
        image.setImageUrl(imageUrl);
        image.setImageType("report");
        image.setUploadedAt(report.getCreatedAt());
        repo.save(image);
    }

    private CollectionRequest ensureCollectionRequest(
            CollectionRequestRepository repo,
            String requestCode,
            WasteReport report,
            Enterprise enterprise,
            Collector collector,
            CollectionRequestStatus status,
            LocalDateTime assignedAt,
            LocalDateTime acceptedAt,
            LocalDateTime startedAt,
            BigDecimal actualWeightKg,
            LocalDateTime collectedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return repo.findByRequestCode(requestCode).orElseGet(() -> {
            CollectionRequest cr = new CollectionRequest();
            cr.setRequestCode(requestCode);
            cr.setReport(report);
            cr.setEnterprise(enterprise);
            cr.setCollector(collector);
            cr.setStatus(status);
            cr.setAssignedAt(assignedAt);
            cr.setAcceptedAt(acceptedAt);
            cr.setStartedAt(startedAt);
            cr.setActualWeightKg(actualWeightKg);
            cr.setCollectedAt(collectedAt);
            cr.setCreatedAt(createdAt);
            cr.setUpdatedAt(updatedAt);
            return repo.save(cr);
        });
    }

    private void ensureTrackingIfMissing(CollectionTrackingRepository repo, CollectionRequest request,
                                         Collector collector, String action, LocalDateTime createdAt) {
        if (request == null || request.getId() == null) return;
        if (repo.existsByCollectionRequest_IdAndAction(request.getId(), action)) return;
        CollectionTracking tracking = new CollectionTracking();
        tracking.setCollectionRequest(request);
        tracking.setCollector(collector);
        tracking.setAction(action);
        tracking.setNote("modular-seed");
        tracking.setCreatedAt(createdAt);
        repo.save(tracking);
    }

    private void ensureCollectorReportIfMissing(
            CollectorReportRepository reportRepository,
            CollectorReportImageRepository imageRepository,
            CollectionRequest request,
            LocalDateTime collectedAt) {
        if (request == null || request.getId() == null) return;
        if (reportRepository.findByCollectionRequestId(request.getId()).isPresent()) return;
        CollectorReport report = new CollectorReport();
        report.setCollectionRequest(request);
        report.setStatus(CollectorReportStatus.COMPLETED);
        report.setCollectorNote("Modular seed completed");
        report.setCollectedAt(collectedAt);
        report.setLatitude(new BigDecimal("10.77653000"));
        report.setLongitude(new BigDecimal("106.70098000"));
        report.setCreatedAt(collectedAt);
        CollectorReport saved = reportRepository.save(report);

        CollectorReportImage img = new CollectorReportImage();
        img.setCollectorReport(saved);
        img.setImageUrl("https://example.com/collectorReports/" + saved.getId() + ".jpg");
        img.setCreatedAt(collectedAt);
        imageRepository.save(img);
    }

    private void ensureFeedback(FeedbackRepository repo, Citizen citizen, CollectionRequest request,
                                LocalDateTime createdAt) {
        String code = "FB-MOD-001";
        if (repo.findByFeedbackCode(code).isPresent()) return;
        Feedback feedback = new Feedback();
        feedback.setFeedbackCode(code);
        feedback.setCitizen(citizen);
        feedback.setCollectionRequest(request);
        feedback.setFeedbackType("COMPLAINT");
        feedback.setSubject("Modular seed feedback");
        feedback.setContent("Seed content");
        feedback.setStatus("pending");
        feedback.setCreatedAt(createdAt);
        feedback.setUpdatedAt(createdAt);
        repo.save(feedback);
    }
}

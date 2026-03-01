package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.WorkRuleProperties;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.TaskAutomationService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAutomationServiceImpl implements TaskAutomationService {

    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorRepository collectorRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;
    private final WasteReportRepository wasteReportRepository;
    private final WorkRuleProperties workRuleProperties;

    @Override
    @Scheduled(fixedRate = 300000) // Check every 5 minutes
    @Transactional
    public void checkAssignedTasksTimeout() {
        int acceptTimeoutHours = workRuleProperties.getAcceptTimeoutHours();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(acceptTimeoutHours);
        List<CollectionRequest> expiredTasks = collectionRequestRepository.findExpiredAssignedTasks(threshold);

        for (CollectionRequest request : expiredTasks) {
            Collector currentCollector = request.getCollector();
            log.info("Task {} assigned > {}h. Processing timeout...", request.getRequestCode(), acceptTimeoutHours);

            if (currentCollector != null) {
                incrementViolation(currentCollector);
                collectorRepository.save(currentCollector);
                logTracking(request.getId(), currentCollector.getId(), "timeout",
                        "Quá hạn nhận việc: chưa nhận việc trong " + acceptTimeoutHours + " giờ");
            }

            if (!isWithinWorkingHours(now)) {
                unassignToEnterprise(request, now,
                        "Tự động hủy phân công: ngoài giờ làm việc, chưa nhận việc trong " + acceptTimeoutHours + " giờ");
                continue;
            }

            boolean reassigned = reassignTask(request, now);
            if (!reassigned) {
                unassignToEnterprise(request, now,
                        "Tự động hủy phân công: không tìm thấy collector phù hợp để tái phân công sau " + acceptTimeoutHours + " giờ");
            }
        }
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Check every hour
    @Transactional
    public void checkSlaViolations() {
        int slaHours = workRuleProperties.getSlaHours();
        LocalDateTime threshold = LocalDateTime.now().minusHours(slaHours);
        List<CollectionRequest> violatedTasks = collectionRequestRepository.findSlaViolatedTasks(threshold);

        for (CollectionRequest request : violatedTasks) {
            if (Boolean.TRUE.equals(request.getSlaViolated())) {
                continue; // Already processed
            }
            log.info("Task {} violated SLA > {}h.", request.getRequestCode(), slaHours);
            processSlaViolation(request);
        }
    }

    private boolean reassignTask(CollectionRequest request, LocalDateTime now) {
        Collector currentCollector = request.getCollector();
        List<Collector> availableCollectors = collectorRepository.findAvailableCollectors(request.getEnterprise().getId());

        Collector bestCollector = null;
        long bestActiveTasks = Long.MAX_VALUE;

        for (Collector c : availableCollectors) {
            if (currentCollector != null && c.getId().equals(currentCollector.getId())) {
                continue;
            }
            long active = collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ASSIGNED)
                    + collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ACCEPTED_COLLECTOR)
                    + collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ON_THE_WAY);
            if (active < bestActiveTasks) {
                bestActiveTasks = active;
                bestCollector = c;
            }
        }

        if (bestCollector != null) {
            request.setCollector(bestCollector);
            request.setAssignedAt(now);
            request.setUpdatedAt(now);
            request.setRejectionReason(null);
            request.setStatus(CollectionRequestStatus.ASSIGNED);
            collectionRequestRepository.save(request);
            log.info("Reassigned task {} to collector {}", request.getRequestCode(), bestCollector.getId());
            logTracking(request.getId(), bestCollector.getId(), "reassigned",
                    "Tự động tái phân công");
            return true;
        } else {
            log.warn("No eligible collector found for reassignment of task {}", request.getRequestCode());
            return false;
        }
    }

    private void processSlaViolation(CollectionRequest request) {
        request.setSlaViolated(true);
        collectionRequestRepository.save(request);

        Collector collector = request.getCollector();
        if (collector != null) {
            incrementViolation(collector);
            if (collector.getViolationCount() != null && collector.getViolationCount() >= workRuleProperties.getSuspendThreshold()) {
                collector.setStatus(CollectorStatus.SUSPEND);
                log.warn("Collector {} suspended due to reaching {} violations.", collector.getId(), collector.getViolationCount());
            }
            collectorRepository.save(collector);
            logTracking(request.getId(), collector.getId(), "sla_violated", "Vi phạm SLA");
        }
    }

    private void incrementViolation(Collector collector) {
        Integer vc = collector.getViolationCount() == null ? 0 : collector.getViolationCount();
        vc = vc + 1;
        collector.setViolationCount(vc);

        int suspendThreshold = workRuleProperties.getSuspendThreshold();
        if (vc >= suspendThreshold) {
            collector.setStatus(CollectorStatus.SUSPEND);
        }
    }

    private boolean isWithinWorkingHours(LocalDateTime now) {
        int start = workRuleProperties.getWorkingStartHour();
        int end = workRuleProperties.getWorkingEndHour();
        int hour = now.getHour();
        if (start == end) {
            return true;
        }
        if (start < end) {
            return hour >= start && hour <= end;
        }
        return hour >= start || hour <= end;
    }

    private void unassignToEnterprise(CollectionRequest request, LocalDateTime now, String reason) {
        request.setCollector(null);
        request.setStatus(CollectionRequestStatus.ACCEPTED_ENTERPRISE);
        request.setAssignedAt(null);
        request.setAcceptedAt(null);
        request.setStartedAt(null);
        request.setRejectionReason(reason);
        request.setUpdatedAt(now);
        collectionRequestRepository.save(request);

        if (request.getReport() != null) {
            request.getReport().setStatus(WasteReportStatus.ACCEPTED_ENTERPRISE);
            request.getReport().setUpdatedAt(now);
            wasteReportRepository.save(request.getReport());
        }
    }

    private void logTracking(Integer requestId, Integer collectorId, String action, String note) {
        CollectionTracking tracking = new CollectionTracking();
        tracking.setCollectionRequest(collectionRequestRepository.getReferenceById(requestId));
        tracking.setCollector(collectorRepository.getReferenceById(collectorId));
        tracking.setAction(action);
        tracking.setNote(note);
        tracking.setCreatedAt(LocalDateTime.now());
        collectionTrackingRepository.save(tracking);
    }
}

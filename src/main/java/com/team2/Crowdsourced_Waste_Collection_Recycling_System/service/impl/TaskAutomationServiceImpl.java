package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.TaskAutomationService;
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

    @Override
    @Scheduled(fixedRate = 300000) // Check every 5 minutes
    @Transactional
    public void checkAssignedTasksTimeout() {
        // Find tasks assigned > 4 hours ago and still ASSIGNED (not ON_THE_WAY)
        LocalDateTime threshold = LocalDateTime.now().minusHours(4);
        List<CollectionRequest> expiredTasks = collectionRequestRepository.findExpiredAssignedTasks(threshold);

        for (CollectionRequest request : expiredTasks) {
            log.info("Task {} assigned > 4h. Reassigning...", request.getRequestCode());
            reassignTask(request);
        }
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Check every hour
    @Transactional
    public void checkSlaViolations() {
        // Find tasks assigned > 72 hours ago and not COMPLETED
        LocalDateTime threshold = LocalDateTime.now().minusHours(72);
        List<CollectionRequest> violatedTasks = collectionRequestRepository.findSlaViolatedTasks(threshold);

        for (CollectionRequest request : violatedTasks) {
            if (Boolean.TRUE.equals(request.getSlaViolated())) {
                continue; // Already processed
            }
            log.info("Task {} violated SLA > 72h.", request.getRequestCode());
            processSlaViolation(request);
        }
    }

    private void reassignTask(CollectionRequest request) {
        // Find nearby active collector (simplistic: any active collector in same enterprise for now)
        Collector currentCollector = request.getCollector();
        // Use the new method to find AVAILABLE and ACTIVE collectors
        List<Collector> availableCollectors = collectorRepository.findAvailableCollectors(request.getEnterprise().getId());

        // Filter by distance if coordinates available
        Collector bestCollector = null;
        double minDistance = 10.0; // 10km radius

        // Request location (from Report)
        if (request.getReport().getLatitude() != null && request.getReport().getLongitude() != null) {
            double reqLat = request.getReport().getLatitude().doubleValue();
            double reqLon = request.getReport().getLongitude().doubleValue();

            for (Collector c : availableCollectors) {
                if (currentCollector != null && c.getId().equals(currentCollector.getId())) continue; // Skip current
                if (c.getCurrentLatitude() == null || c.getCurrentLongitude() == null) continue;

                double dist = calculateDistance(reqLat, reqLon, c.getCurrentLatitude().doubleValue(), c.getCurrentLongitude().doubleValue());
                if (dist < minDistance) {
                    minDistance = dist;
                    bestCollector = c;
                }
            }
        }

        if (bestCollector != null) {
            request.setCollector(bestCollector);
            request.setAssignedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            // Reset status to ASSIGNED if it was somehow changed or to ensure consistency
            request.setStatus(CollectionRequestStatus.ASSIGNED);
            collectionRequestRepository.save(request);
            log.info("Reassigned task {} to collector {}", request.getRequestCode(), bestCollector.getId());
        } else {
            log.warn("No available collector found for reassignment of task {}", request.getRequestCode());
        }
    }

    private void processSlaViolation(CollectionRequest request) {
        request.setSlaViolated(true);
        collectionRequestRepository.save(request);

        Collector collector = request.getCollector();
        if (collector != null) {
            collector.setViolationCount(collector.getViolationCount() == null ? 1 : collector.getViolationCount() + 1);
            if (collector.getViolationCount() >= 3) {
                collector.setStatus(CollectorStatus.SUSPEND);
                log.warn("Collector {} suspended due to 3 violations.", collector.getId());
            }
            collectorRepository.save(collector);
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

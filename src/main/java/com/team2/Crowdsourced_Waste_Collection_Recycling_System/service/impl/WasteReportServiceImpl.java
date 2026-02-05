package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteType;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.WasteTypeRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WasteReportServiceImpl implements WasteReportService {

        private final WasteReportRepository wasteReportRepository;
        private final CitizenRepository citizenRepository;
        private final WasteTypeRepository wasteTypeRepository;
        private final ReportImageRepository reportImagesRepository;
        private final CloudinaryService cloudinaryService;

        @Override
        @Transactional
        public WasteReportResponse createReport(CreateWasteReportRequest request, String citizenEmail) {
                // 1. Authenticate Citizen
                Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                                .orElseThrow(() -> new AppException(ErrorCode.CITIZEN_NOT_FOUND));

                // 2. Check daily limit
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
                long todayReports = wasteReportRepository.countByCitizen_IdAndCreatedAtBetween(citizen.getId(),
                                startOfDay,
                                endOfDay);

                if (todayReports >= 5) {
                        log.warn("Citizen {} exceeded daily report limit", citizen.getId());
                        throw new AppException(ErrorCode.DAILY_REPORT_LIMIT_EXCEEDED);
                }

                // 3. Validate and fetch WasteTypes
                List<WasteType> wasteTypes = new ArrayList<>();
                for (String wasteTypeCode : request.getWasteTypes()) {
                        WasteType wasteType = wasteTypeRepository.findByCode(wasteTypeCode)
                                        .or(() -> wasteTypeRepository.findByName(wasteTypeCode))
                                        .orElseThrow(() -> new AppException(ErrorCode.WASTE_TYPE_NOT_FOUND));
                        wasteTypes.add(wasteType);
                }

                // 4. Upload Image to Cloudinary
                CloudinaryResponse cloudinaryResponse = cloudinaryService.uploadImage(request.getImage(), "reports");

                // 5. Create WasteReport
                WasteReport report = new WasteReport();
                report.setCitizen(citizen);
                report.getWasteTypes().addAll(wasteTypes);
                report.setDescription(request.getDescription());
                report.setEstimatedWeight(request.getEstimatedWeight());
                report.setLatitude(BigDecimal.valueOf(request.getLatitude()));
                report.setLongitude(BigDecimal.valueOf(request.getLongitude()));
                report.setStatus("PENDING");
                report.setImages(cloudinaryResponse.getUrl());
                report.setCloudinaryPublicId(cloudinaryResponse.getPublicId());

                // Generate Report Code
                String reportCode = "WR-" + System.currentTimeMillis() + "-"
                                + UUID.randomUUID().toString().substring(0, 4);
                report.setReportCode(reportCode);

                // Set creation time
                report.setCreatedAt(LocalDateTime.now());
                report.setUpdatedAt(LocalDateTime.now());

                // Save Report
                report = wasteReportRepository.save(report);
                log.info("Waste report created: {} by citizen: {}", reportCode, citizenEmail);

                // 6. Save ReportImage
                ReportImage reportImage = new ReportImage();
                reportImage.setReport(report);
                reportImage.setImageUrl(cloudinaryResponse.getUrl());
                reportImage.setImageType("report");
                reportImagesRepository.save(reportImage);

                // 7. Return Response
                return WasteReportResponse.builder()
                                .id(report.getId())
                                .message("Report created successfully")
                                .reportCode(reportCode)
                                .status("PENDING")
                                .createdAt(report.getCreatedAt())
                                .build();
        }

        @Override
        public List<WasteReportResponse> getMyReports(String citizenEmail) {
                // 1. Find citizen by email
                Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

                // 2. Get all reports by citizen ID
                List<WasteReport> reports = wasteReportRepository.findByCitizen_Id(citizen.getId());

                // 3. Convert to response DTOs
                return reports.stream()
                                .map(report -> WasteReportResponse.builder()
                                                .id(report.getId())
                                                .reportCode(report.getReportCode())
                                                .status(report.getStatus())
                                                .createdAt(report.getCreatedAt())
                                                .build())
                                .toList();
        }
}

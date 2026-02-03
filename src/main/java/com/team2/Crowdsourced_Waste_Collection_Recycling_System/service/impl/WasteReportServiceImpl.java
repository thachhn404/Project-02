package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteType;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.citizen.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.citizen.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.citizen.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.citizen.WasteTypeRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WasteReportServiceImpl implements WasteReportService {

    private final WasteReportRepository wasteReportRepository;
    private final CitizenRepository citizenRepository;
    private final WasteTypeRepository wasteTypeRepository;
    private final ReportImageRepository reportImagesRepository;

    private static final String UPLOAD_DIR = "uploads";

    @Override
    @Transactional
    public WasteReportResponse createReport(CreateWasteReportRequest request, String citizenEmail) {
        // 1. Authenticate Citizen
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.CITIZEN_NOT_FOUND));

        // 2. Check daily limit
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        long todayReports = wasteReportRepository.countByCitizen_IdAndCreatedAtBetween(citizen.getId(), startOfDay, endOfDay);

        if (todayReports >= 5) {
            log.warn("Citizen {} exceeded daily report limit", citizen.getId());
            throw new AppException(ErrorCode.DAILY_REPORT_LIMIT_EXCEEDED);
        }

        // 3. Validate inputs (WasteType)
        WasteType wasteType = wasteTypeRepository.findByCode(request.getWasteType())
                .or(() -> wasteTypeRepository.findByName(request.getWasteType()))
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_TYPE_NOT_FOUND));

        // 4. Upload Image
        String imageUrl = uploadImage(request.getImage());

        // 5. Create WasteReport
        WasteReport report = new WasteReport();
        report.setCitizen(citizen);
        report.setWasteType(wasteType);
        report.setDescription(request.getDescription());
        report.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        report.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        report.setStatus("PENDING");
        report.setImages(imageUrl); // Saving URL to images column
        
        // Generate Report Code
        String reportCode = "WR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
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
        reportImage.setImageUrl(imageUrl);
        reportImage.setImageType("report");
        reportImagesRepository.save(reportImage);

        // 7. Return Response
        return WasteReportResponse.builder()
                .id(report.getId())
                .message("Report created successfully")
                .reportCode(reportCode)
                .status("PENDING")
                .build();
    }

    private String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}

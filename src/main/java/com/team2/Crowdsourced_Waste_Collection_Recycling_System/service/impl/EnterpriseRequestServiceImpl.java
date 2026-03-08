package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnterpriseRequestServiceImpl implements EnterpriseRequestService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final WasteReportRepository wasteReportRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Override
    @Transactional
    public Integer acceptWasteReport(Integer enterpriseId, String reportCode) {
        return acceptWasteReport(enterpriseId, reportCode, null);
    }

    @Override
    @Transactional
    public Integer acceptWasteReport(Integer enterpriseId, String reportCode, java.math.BigDecimal estimatedWeight) {
        // Kiểm tra mã báo cáo
        if (reportCode == null || reportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu report_code");
        }

        // Tìm báo cáo rác trong DB
        WasteReport wasteReport = wasteReportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waste Report không tồn tại"));
        
        // Kiểm tra doanh nghiệp
        Enterprise enterprise = requireEnterprise(enterpriseId);

        // Logic xử lý:
        // 1. Nếu báo cáo chưa được xử lý (PENDING) -> Chấp nhận và tạo request mới
        // 2. Nếu đã được doanh nghiệp này chấp nhận (ACCEPTED_ENTERPRISE) -> Trả về ID request cũ
        // 3. Các trường hợp khác -> Báo lỗi

        LocalDateTime now = LocalDateTime.now();

        // Trường hợp 1: Báo cáo đang chờ xử lý hoặc chưa có trạng thái
        if (wasteReport.getStatus() == null || wasteReport.getStatus() == WasteReportStatus.PENDING) {
            
            // Kiểm tra xem địa chỉ có nằm trong khu vực phục vụ không
            if (!isInServiceArea(enterprise, wasteReport)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Báo cáo nằm ngoài khu vực phục vụ của doanh nghiệp");
            }

            // Kiểm tra xem đã tồn tại request nào cho báo cáo này chưa (để tránh trùng lặp)
            if (collectionRequestRepository.existsByReport_Id(wasteReport.getId())) {
                CollectionRequest existingRequest = collectionRequestRepository.findByReport_Id(wasteReport.getId()).orElse(null);
                if (existingRequest != null) {
                    return existingRequest.getId();
                } else {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Trạng thái không nhất quán: Có request nhưng không tìm thấy");
                }
            }

            // Cập nhật trạng thái báo cáo
            wasteReport.setStatus(WasteReportStatus.ACCEPTED_ENTERPRISE);
            wasteReport.setAcceptedAt(now);
            wasteReport.setUpdatedAt(now);
            wasteReportRepository.save(wasteReport);

            // Tạo request thu gom mới
            CollectionRequest cr = new CollectionRequest();
            cr.setRequestCode(generateRequestCode());
            cr.setReport(wasteReport);
            cr.setEnterprise(enterprise);
            cr.setStatus(CollectionRequestStatus.ACCEPTED_ENTERPRISE);
            cr.setCreatedAt(now);
            cr.setUpdatedAt(now);
            cr.setSlaViolated(false);
            
            // Lưu lần 1 để có ID
            collectionRequestRepository.save(cr);
            
            // Cập nhật lại mã request theo format chuẩn (CR + ID)
            cr.setRequestCode(String.format("CR%03d", cr.getId()));
            collectionRequestRepository.save(cr);

            return cr.getId();

        } 
        // Trường hợp 2: Đã được chấp nhận trước đó
        else if (wasteReport.getStatus() == WasteReportStatus.ACCEPTED_ENTERPRISE) {
            CollectionRequest existing = collectionRequestRepository.findByReport_Id(wasteReport.getId()).orElse(null);
            
            if (existing != null) {
                // Kiểm tra và cập nhật lại mã request nếu chưa đúng format
                if (existing.getRequestCode() == null || !existing.getRequestCode().matches("^CR\\d{3}$")) {
                    existing.setRequestCode(String.format("CR%03d", existing.getId()));
                    collectionRequestRepository.save(existing);
                }
                return existing.getId();
            } else {
                // Nếu trạng thái là ACCEPTED nhưng chưa có request (lỗi dữ liệu cũ?), tạo mới
                CollectionRequest cr = new CollectionRequest();
                cr.setRequestCode(generateRequestCode());
                cr.setReport(wasteReport);
                cr.setEnterprise(enterprise);
                cr.setStatus(CollectionRequestStatus.ACCEPTED_ENTERPRISE);
                cr.setCreatedAt(now);
                cr.setUpdatedAt(now);
                cr.setSlaViolated(false);
                collectionRequestRepository.save(cr);
                
                cr.setRequestCode(String.format("CR%03d", cr.getId()));
                collectionRequestRepository.save(cr);
                
                return cr.getId();
            }
        } 
        // Trường hợp 3: Trạng thái không hợp lệ
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Waste Report không ở trạng thái hợp lệ để accept");
        }
    }

    @Override
    @Transactional
    public void rejectWasteReport(Integer enterpriseId, String reportCode, String reason) {
        if (reportCode == null || reportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu report_code");
        }
        WasteReport wasteReport = wasteReportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waste Report không tồn tại"));
        wasteReport.setStatus(WasteReportStatus.REJECTED);
        wasteReport.setRejectionReason(reason);
        wasteReport.setUpdatedAt(LocalDateTime.now());
        wasteReportRepository.save(wasteReport);
    }

    /**
     * Generate unique request code with format: CR-YYYYMMDD-XXXXX
     */
    private String generateRequestCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%05d", new Random().nextInt(100000));
        String requestCode = "CR-" + dateStr + "-" + randomStr;

        // Check if code already exists, regenerate if needed
        while (collectionRequestRepository.findByRequestCode(requestCode).isPresent()) {
            randomStr = String.format("%05d", new Random().nextInt(100000));
            requestCode = "CR-" + dateStr + "-" + randomStr;
        }

        return requestCode;
    }

    private boolean isInServiceArea(Enterprise enterprise, WasteReport report) {
        String address = report.getAddress();
        if (address == null || address.isBlank()) {
            return false;
        }

        // Lấy danh sách phường/xã và quận/huyện phục vụ
        String wardList = enterprise.getServiceWards();
        String cityList = enterprise.getServiceCities();
        String lowerAddress = address.toLowerCase();

        // Kiểm tra phường/xã (nếu không cấu hình thì coi như ok)
        boolean wardOk = false;
        if (wardList == null || wardList.isBlank()) {
            wardOk = true;
        } else {
            // Tách chuỗi bằng dấu phẩy và duyệt từng phần tử
            String[] wards = wardList.split(",");
            for (String ward : wards) {
                String cleanWard = ward.trim();
                if (!cleanWard.isEmpty() && lowerAddress.contains(cleanWard.toLowerCase())) {
                    wardOk = true;
                    break;
                }
            }
        }

        // Kiểm tra quận/huyện/thành phố (nếu không cấu hình thì coi như ok)
        boolean cityOk = false;
        if (cityList == null || cityList.isBlank()) {
            cityOk = true;
        } else {
            // Tách chuỗi bằng dấu phẩy và duyệt từng phần tử
            String[] cities = cityList.split(",");
            for (String city : cities) {
                String cleanCity = city.trim();
                if (!cleanCity.isEmpty() && lowerAddress.contains(cleanCity.toLowerCase())) {
                    cityOk = true;
                    break;
                }
            }
        }

        return wardOk && cityOk;
    }

    private Enterprise requireEnterprise(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));
    }
}

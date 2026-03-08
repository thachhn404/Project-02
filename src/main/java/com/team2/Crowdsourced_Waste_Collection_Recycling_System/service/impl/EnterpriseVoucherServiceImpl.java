package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateVoucherRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateVoucherRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseVoucherResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Voucher;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.VoucherRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward.EnterpriseVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseVoucherServiceImpl implements EnterpriseVoucherService {
    private final VoucherRepository voucherRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public List<EnterpriseVoucherResponse> list(Boolean active) {
        // Lấy tất cả voucher sắp xếp theo ID giảm dần
        List<Voucher> allVouchers = voucherRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        
        List<EnterpriseVoucherResponse> result = new ArrayList<>();
        
        for (Voucher voucher : allVouchers) {
            // Nếu có yêu cầu lọc theo trạng thái active
            if (active != null) {
                if (!active.equals(voucher.getActive())) {
                    continue; // Bỏ qua nếu không khớp trạng thái
                }
            }
            
            // Chuyển đổi và thêm vào danh sách kết quả
            result.add(toResponse(voucher));
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EnterpriseVoucherResponse getById(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher không tồn tại"));
        return toResponse(voucher);
    }

    @Override
    @Transactional
    public EnterpriseVoucherResponse create(CreateVoucherRequest request) {
        validateCreate(request);

        voucherRepository.findByTitleIgnoreCase(request.getTitle().trim())
                .ifPresent(v -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Title voucher đã tồn tại");
                });

        Voucher voucher = new Voucher();
        voucher.setTitle(request.getTitle().trim());
        voucher.setValueDisplay(trimToNull(request.getValueDisplay()));
        voucher.setPointsRequired(request.getPointsRequired());
        voucher.setValidUntil(request.getValidUntil());
        voucher.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        voucher.setRemainingStock(request.getRemainingStock());
        voucher.setTerms(normalizeTerms(request.getTerms()));
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());

        Voucher saved = voucherRepository.save(voucher);
        if (saved.getId() != null && (saved.getVoucherCode() == null || saved.getVoucherCode().isBlank())) {
            saved.setVoucherCode(formatVoucherCode(saved.getId()));
            saved.setUpdatedAt(LocalDateTime.now());
            saved = voucherRepository.save(saved);
        }

        boolean updated = false;
        if (request.getBanner() != null && !request.getBanner().isEmpty()) {
            CloudinaryResponse uploaded = cloudinaryService.uploadImage(request.getBanner(), "vouchers");
            if (uploaded == null || uploaded.getUrl() == null || uploaded.getUrl().isBlank()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload banner thất bại");
            }
            saved.setBannerUrl(uploaded.getUrl());
            saved.setBannerPublicId(uploaded.getPublicId());
            updated = true;
        }
        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            CloudinaryResponse uploaded = cloudinaryService.uploadImage(request.getLogo(), "vouchers");
            if (uploaded == null || uploaded.getUrl() == null || uploaded.getUrl().isBlank()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload logo thất bại");
            }
            saved.setLogoUrl(uploaded.getUrl());
            saved.setLogoPublicId(uploaded.getPublicId());
            updated = true;
        }
        if (updated) {
            saved.setUpdatedAt(LocalDateTime.now());
            saved = voucherRepository.save(saved);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public EnterpriseVoucherResponse update(Integer id, UpdateVoucherRequest request) {
        validateUpdate(request);

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher không tồn tại"));

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title không được để trống");
            }
            if (!title.equalsIgnoreCase(voucher.getTitle())) {
                voucherRepository.findByTitleIgnoreCase(title)
                        .filter(v -> !v.getId().equals(voucher.getId()))
                        .ifPresent(v -> {
                            throw new ResponseStatusException(HttpStatus.CONFLICT, "Title voucher đã tồn tại");
                        });
                voucher.setTitle(title);
            }
        }

        if (request.getValueDisplay() != null) {
            voucher.setValueDisplay(trimToNull(request.getValueDisplay()));
        }
        if (request.getPointsRequired() != null) {
            voucher.setPointsRequired(request.getPointsRequired());
        }
        if (request.getValidUntil() != null) {
            voucher.setValidUntil(request.getValidUntil());
        }
        if (request.getActive() != null) {
            voucher.setActive(request.getActive());
        }
        if (request.getRemainingStock() != null) {
            voucher.setRemainingStock(request.getRemainingStock());
        }
        if (request.getTerms() != null) {
            voucher.setTerms(normalizeTerms(request.getTerms()));
        }
        if (request.getBannerUrl() != null) {
            voucher.setBannerUrl(trimToNull(request.getBannerUrl()));
        }
        if (request.getLogoUrl() != null) {
            voucher.setLogoUrl(trimToNull(request.getLogoUrl()));
        }

        voucher.setUpdatedAt(LocalDateTime.now());
        Voucher saved = voucherRepository.save(voucher);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher không tồn tại"));

        voucher.setActive(Boolean.FALSE);
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
    }

    private static EnterpriseVoucherResponse toResponse(Voucher voucher) {
        return EnterpriseVoucherResponse.builder()
                .id(voucher.getId())
                .voucherCode(voucher.getVoucherCode() != null ? voucher.getVoucherCode() : formatVoucherCode(voucher.getId()))
                .bannerUrl(voucher.getBannerUrl())
                .logoUrl(voucher.getLogoUrl())
                .title(voucher.getTitle())
                .valueDisplay(voucher.getValueDisplay())
                .pointsRequired(voucher.getPointsRequired())
                .validUntil(voucher.getValidUntil())
                .active(voucher.getActive())
                .remainingStock(voucher.getRemainingStock())
                .terms(voucher.getTerms())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }

    private static String formatVoucherCode(Integer id) {
        if (id == null) return null;
        return String.format("V%03d", id);
    }

    private static void validateCreate(CreateVoucherRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title không được để trống");
        }
        if (request.getPointsRequired() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pointsRequired không được để trống");
        }
        if (request.getPointsRequired() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pointsRequired không hợp lệ");
        }
        if (request.getRemainingStock() != null && request.getRemainingStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "remainingStock không hợp lệ");
        }
    }

    private static void validateUpdate(UpdateVoucherRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu");
        }
        boolean hasAnyField = request.getTitle() != null
                || request.getValueDisplay() != null
                || request.getPointsRequired() != null
                || request.getValidUntil() != null
                || request.getActive() != null
                || request.getRemainingStock() != null
                || request.getTerms() != null
                || request.getBannerUrl() != null
                || request.getLogoUrl() != null;
        if (!hasAnyField) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không có trường nào để cập nhật");
        }
        if (request.getPointsRequired() != null && request.getPointsRequired() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pointsRequired không hợp lệ");
        }
        if (request.getRemainingStock() != null && request.getRemainingStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "remainingStock không hợp lệ");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static List<String> normalizeTerms(List<String> terms) {
        if (terms == null) return new ArrayList<>();
        return terms.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }
}

package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.VoucherRedemptionResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.VoucherResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointTransaction;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Voucher;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.VoucherRedemption;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.VoucherRedemptionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.VoucherRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherRedemptionRepository voucherRedemptionRepository;
    private final CitizenRepository citizenRepository;
    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    public List<VoucherResponse> getAvailableVouchers() {
        // Lấy danh sách voucher đang hoạt động
        List<Voucher> vouchers = voucherRepository.findAllByActiveTrueOrderByIdDesc();
        
        // Chuyển đổi sang response
        List<VoucherResponse> responses = new ArrayList<>();
        for (Voucher v : vouchers) {
            responses.add(toVoucherResponse(v));
        }
        return responses;
    }

    @Override
    @Transactional
    public VoucherRedemptionResponse redeem(Integer voucherId, String citizenEmail) {
        // 1. Tìm voucher và khóa dòng để tránh lỗi khi nhiều người cùng đổi
        Optional<Voucher> voucherOpt = voucherRepository.findByIdForUpdate(voucherId);
        if (voucherOpt.isEmpty()) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }
        Voucher voucher = voucherOpt.get();

        // 2. Kiểm tra điều kiện đổi voucher
        validateVoucherRedeemable(voucher);

        // 3. Lấy thông tin công dân
        Optional<Citizen> citizenOpt = citizenRepository.findByUser_Email(citizenEmail);
        if (citizenOpt.isEmpty()) {
            throw new AppException(ErrorCode.CITIZEN_NOT_FOUND);
        }
        Citizen citizen = citizenOpt.get();

        // 4. Khóa thông tin công dân để cập nhật điểm an toàn
        Optional<Citizen> lockedCitizenOpt = citizenRepository.findByIdForUpdate(citizen.getId());
        if (lockedCitizenOpt.isEmpty()) {
            throw new AppException(ErrorCode.CITIZEN_NOT_FOUND);
        }
        Citizen lockedCitizen = lockedCitizenOpt.get();

        // 5. Kiểm tra điểm tích lũy
        int currentPoints = lockedCitizen.getTotalPoints() != null ? lockedCitizen.getTotalPoints() : 0;
        int cost = voucher.getPointsRequired() != null ? voucher.getPointsRequired() : 0;
        
        if (currentPoints < cost) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        // 6. Kiểm tra số lượng tồn kho
        Integer stock = voucher.getRemainingStock();
        if (stock != null && stock <= 0) {
            throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
        }

        // 7. Trừ điểm công dân
        int balanceAfter = currentPoints - cost;
        lockedCitizen.setTotalPoints(balanceAfter);
        citizenRepository.save(lockedCitizen);

        // 8. Trừ tồn kho voucher (nếu có giới hạn)
        if (stock != null) {
            voucher.setRemainingStock(stock - 1);
            voucherRepository.save(voucher);
        }

        // 9. Lưu lịch sử đổi voucher
        VoucherRedemption redemption = new VoucherRedemption();
        redemption.setCitizen(lockedCitizen);
        redemption.setVoucher(voucher);
        redemption.setRedemptionCode(generateRedemptionCode());
        redemption.setPointsSpent(cost);
        redemption.setStatus("ACTIVE");
        redemption.setRedeemedAt(LocalDateTime.now());
        VoucherRedemption savedRedemption = voucherRedemptionRepository.save(redemption);

        // 10. Lưu lịch sử giao dịch trừ điểm
        User user = userRepository.findByEmail(citizenEmail).orElse(null);
        if (user == null) {
             throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        PointTransaction tx = new PointTransaction();
        tx.setCitizen(lockedCitizen);
        tx.setPoints(-cost); // Điểm âm vì là tiêu dùng
        tx.setTransactionType("SPEND_VOUCHER");
        tx.setDescription("Đổi voucher #" + voucher.getId() + " - " + voucher.getTitle());
        tx.setBalanceAfter(balanceAfter);
        tx.setCreatedBy(user);
        tx.setCreatedAt(LocalDateTime.now());
        pointTransactionRepository.save(tx);

        return toVoucherRedemptionResponse(savedRedemption, balanceAfter);
    }

    @Override
    public List<VoucherRedemptionResponse> getMyVouchers(String citizenEmail) {
        Optional<Citizen> citizenOpt = citizenRepository.findByUser_Email(citizenEmail);
        if (citizenOpt.isEmpty()) {
            throw new AppException(ErrorCode.CITIZEN_NOT_FOUND);
        }
        Citizen citizen = citizenOpt.get();

        List<VoucherRedemption> redemptions = voucherRedemptionRepository.findAllByCitizen_IdOrderByRedeemedAtDesc(citizen.getId());
        
        List<VoucherRedemptionResponse> responses = new ArrayList<>();
        for (VoucherRedemption r : redemptions) {
            responses.add(toVoucherRedemptionResponse(r, null));
        }
        return responses;
    }

    private void validateVoucherRedeemable(Voucher voucher) {
        if (voucher.getActive() == null || !voucher.getActive()) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }

        LocalDate validUntil = voucher.getValidUntil();
        if (validUntil != null && validUntil.isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED);
        }
    }

    private String generateRedemptionCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private VoucherResponse toVoucherResponse(Voucher voucher) {
        String code = voucher.getVoucherCode();
        if (code == null) {
            code = formatVoucherCode(voucher.getId());
        }
        
        return VoucherResponse.builder()
                .id(voucher.getId())
                .voucherCode(code)
                .bannerUrl(voucher.getBannerUrl())
                .logoUrl(voucher.getLogoUrl())
                .value(voucher.getValueDisplay())
                .title(voucher.getTitle())
                .validUntil(voucher.getValidUntil())
                .pointsRequired(voucher.getPointsRequired())
                .terms(voucher.getTerms())
                .build();
    }

    private static String formatVoucherCode(Integer id) {
        if (id == null) return null;
        return String.format("V%03d", id);
    }

    private VoucherRedemptionResponse toVoucherRedemptionResponse(VoucherRedemption redemption, Integer balanceAfter) {
        return VoucherRedemptionResponse.builder()
                .id(redemption.getId())
                .voucher(toVoucherResponse(redemption.getVoucher()))
                .redemptionCode(redemption.getRedemptionCode())
                .pointsSpent(redemption.getPointsSpent())
                .balanceAfter(balanceAfter)
                .status(redemption.getStatus())
                .redeemedAt(redemption.getRedeemedAt())
                .build();
    }
}

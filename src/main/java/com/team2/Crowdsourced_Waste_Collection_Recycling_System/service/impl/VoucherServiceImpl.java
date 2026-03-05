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
import java.util.List;
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
        return voucherRepository.findAllByActiveTrueOrderByIdDesc().stream()
                .map(this::toVoucherResponse)
                .toList();
    }

    @Override
    @Transactional
    public VoucherRedemptionResponse redeem(Integer voucherId, String citizenEmail) {
        Voucher voucher = voucherRepository.findByIdForUpdate(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        validateVoucherRedeemable(voucher);

        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.CITIZEN_NOT_FOUND));

        Citizen lockedCitizen = citizenRepository.findByIdForUpdate(citizen.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CITIZEN_NOT_FOUND));

        int currentPoints = lockedCitizen.getTotalPoints() != null ? lockedCitizen.getTotalPoints() : 0;
        int cost = voucher.getPointsRequired() != null ? voucher.getPointsRequired() : 0;
        if (currentPoints < cost) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        Integer stock = voucher.getRemainingStock();
        if (stock != null && stock <= 0) {
            throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
        }

        int balanceAfter = currentPoints - cost;
        lockedCitizen.setTotalPoints(balanceAfter);
        citizenRepository.save(lockedCitizen);

        if (stock != null) {
            voucher.setRemainingStock(stock - 1);
            voucherRepository.save(voucher);
        }

        VoucherRedemption redemption = new VoucherRedemption();
        redemption.setCitizen(lockedCitizen);
        redemption.setVoucher(voucher);
        redemption.setRedemptionCode(generateRedemptionCode());
        redemption.setPointsSpent(cost);
        redemption.setStatus("ACTIVE");
        redemption.setRedeemedAt(LocalDateTime.now());
        VoucherRedemption savedRedemption = voucherRedemptionRepository.save(redemption);

        User user = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PointTransaction tx = new PointTransaction();
        tx.setCitizen(lockedCitizen);
        tx.setPoints(-cost);
        tx.setTransactionType("SPEND_VOUCHER");
        tx.setDescription("Redeem voucher #" + voucher.getId() + " - " + voucher.getTitle());
        tx.setBalanceAfter(balanceAfter);
        tx.setCreatedBy(user);
        tx.setCreatedAt(LocalDateTime.now());
        pointTransactionRepository.save(tx);

        return toVoucherRedemptionResponse(savedRedemption, balanceAfter);
    }

    @Override
    public List<VoucherRedemptionResponse> getMyVouchers(String citizenEmail) {
        Citizen citizen = citizenRepository.findByUser_Email(citizenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.CITIZEN_NOT_FOUND));

        return voucherRedemptionRepository.findAllByCitizen_IdOrderByRedeemedAtDesc(citizen.getId()).stream()
                .map(r -> toVoucherRedemptionResponse(r, null))
                .toList();
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
        return VoucherResponse.builder()
                .id(voucher.getId())
                .voucherCode(voucher.getVoucherCode() != null ? voucher.getVoucherCode() : formatVoucherCode(voucher.getId()))
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

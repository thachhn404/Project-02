package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.VoucherRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Integer> {
    List<VoucherRedemption> findAllByCitizen_IdOrderByRedeemedAtDesc(Integer citizenId);

    boolean existsByRedemptionCodeIgnoreCase(String redemptionCode);
}

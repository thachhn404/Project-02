package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.VoucherRedemptionResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.VoucherResponse;

import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getAvailableVouchers();

    VoucherRedemptionResponse redeem(Integer voucherId, String citizenEmail);

    List<VoucherRedemptionResponse> getMyVouchers(String citizenEmail);
}

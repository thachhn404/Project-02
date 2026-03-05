package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateVoucherRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateVoucherRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseVoucherResponse;

import java.util.List;

public interface EnterpriseVoucherService {
    List<EnterpriseVoucherResponse> list(Boolean active);

    EnterpriseVoucherResponse getById(Integer id);

    EnterpriseVoucherResponse create(CreateVoucherRequest request);

    EnterpriseVoucherResponse update(Integer id, UpdateVoucherRequest request);

    void softDelete(Integer id);
}

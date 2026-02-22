package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RejectWasteReportRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    String rejectionReason;
}

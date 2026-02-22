package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCollectorReportRequest {
    Integer collectionRequestId;

    @NotBlank(message = "Loại rác là bắt buộc")
    String wasteType;

    @Valid
    @NotEmpty(message = "Danh sách mục thu gom là bắt buộc")
    List<CollectorReportItemRequest> items;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự.")
    String collectorNote;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự.")
    String address;

    Double latitude;
    Double longitude;
}

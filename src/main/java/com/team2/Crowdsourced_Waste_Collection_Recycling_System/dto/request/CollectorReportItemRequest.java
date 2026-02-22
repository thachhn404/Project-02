package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectorReportItemRequest {
    Integer categoryId;

    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    String categoryName;

    @NotNull(message = "Số lượng là bắt buộc")
    @DecimalMin(value = "0.0001", inclusive = true, message = "Số lượng phải lớn hơn 0")
    @Digits(integer = 19, fraction = 4, message = "Số lượng tối đa 4 chữ số thập phân")
    BigDecimal quantity;

    @AssertTrue(message = "Cần cung cấp categoryId hoặc categoryName")
    public boolean isCategoryReferenceValid() {
        boolean hasId = categoryId != null;
        boolean hasName = categoryName != null && !categoryName.trim().isEmpty();
        return hasId ^ hasName;
    }
}

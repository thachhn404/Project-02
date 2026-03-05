package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateVoucherRequest {
    String title;
    String valueDisplay;
    Integer pointsRequired;
    LocalDate validUntil;
    Boolean active;
    Integer remainingStock;
    List<String> terms;
    MultipartFile banner;
    MultipartFile logo;
}

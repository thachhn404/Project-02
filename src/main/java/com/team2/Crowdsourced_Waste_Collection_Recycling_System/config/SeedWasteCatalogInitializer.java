package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@ConditionalOnProperty(name = "app.seed.modular", havingValue = "true")
public class SeedWasteCatalogInitializer {

    @Bean
    public CommandLineRunner seedWasteCategories(WasteCategoryRepository wasteCategoryRepository) {
        return args -> {
            LocalDateTime now = LocalDateTime.now();
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Giấy", WasteUnit.KG, new BigDecimal("2250.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Báo", WasteUnit.KG, new BigDecimal("3600.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Giấy, hồ sơ", WasteUnit.KG, new BigDecimal("3150.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Giấy tập", WasteUnit.KG, new BigDecimal("3600.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Lon bia", WasteUnit.CAN, new BigDecimal("180.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Sắt", WasteUnit.KG, new BigDecimal("3600.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Sắt lon", WasteUnit.KG, new BigDecimal("1440.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Inox", WasteUnit.KG, new BigDecimal("5400.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Đồng", WasteUnit.KG, new BigDecimal("67500.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Nhôm", WasteUnit.KG, new BigDecimal("16200.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Chai thủy tinh", WasteUnit.BOTTLE, new BigDecimal("450.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Bao bì, hỗn hợp", WasteUnit.KG, new BigDecimal("1600.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Meca", WasteUnit.KG, new BigDecimal("450.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Mủ", WasteUnit.KG, new BigDecimal("3600.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Mủ bình", WasteUnit.KG, new BigDecimal("4500.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Mủ tôn", WasteUnit.KG, new BigDecimal("1800.0000"), now);
            createWasteCategoryIfNotFound(wasteCategoryRepository, "Mủ đen", WasteUnit.KG, new BigDecimal("150.0000"), now);
        };
    }

    private WasteCategory createWasteCategoryIfNotFound(
            WasteCategoryRepository repo,
            String name,
            WasteUnit unit,
            BigDecimal pointPerUnit,
            LocalDateTime now) {
        return repo.findByNameIgnoreCase(name).orElseGet(() -> {
            WasteCategory category = new WasteCategory();
            category.setName(name);
            category.setUnit(unit);
            category.setPointPerUnit(pointPerUnit);
            category.setCreatedAt(now);
            category.setUpdatedAt(now);
            return repo.save(category);
        });
    }
}


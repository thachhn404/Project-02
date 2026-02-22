package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class RecyclableRewardCalculator {
    public CalculationResult calculate(List<ItemInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("inputs is required");
        }
        int totalPoint = 0;
        BigDecimal totalWeightKg = BigDecimal.ZERO;
        List<ItemResult> results = new ArrayList<>();
        for (ItemInput input : inputs) {
            WasteCategory category = input.category();
            BigDecimal quantity = input.quantity();
            int point = calculateItemPoint(quantity, category.getPointPerUnit());
            totalPoint = safeAddPoint(totalPoint, point);
            if (category.getUnit() == WasteUnit.KG) {
                totalWeightKg = totalWeightKg.add(quantity);
            }
            results.add(new ItemResult(category, quantity, point));
        }
        return new CalculationResult(totalPoint, totalWeightKg, results);
    }

    private int calculateItemPoint(BigDecimal quantity, BigDecimal pointPerUnit) {
        BigDecimal raw = quantity.multiply(pointPerUnit);
        BigDecimal rounded = raw.setScale(0, RoundingMode.HALF_UP);
        return rounded.intValueExact();
    }

    private int safeAddPoint(int total, int add) {
        long sum = (long) total + (long) add;
        if (sum > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("totalPoint too large");
        }
        return (int) sum;
    }

    public record ItemInput(WasteCategory category, BigDecimal quantity) { }

    public record ItemResult(WasteCategory category, BigDecimal quantity, int point) { }

    public record CalculationResult(int totalPoint, BigDecimal totalWeightKg, List<ItemResult> items) { }
}


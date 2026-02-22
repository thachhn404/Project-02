package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecyclableRewardCalculatorTest {
    @Test
    void calculatesTotalAndItemPointsWithMixedUnits() {
        WasteCategory paper = new WasteCategory();
        paper.setId(1);
        paper.setName("Giấy");
        paper.setUnit(WasteUnit.KG);
        paper.setPointPerUnit(new BigDecimal("2250"));

        WasteCategory copper = new WasteCategory();
        copper.setId(9);
        copper.setName("Đồng");
        copper.setUnit(WasteUnit.KG);
        copper.setPointPerUnit(new BigDecimal("67500"));

        WasteCategory can = new WasteCategory();
        can.setId(5);
        can.setName("Lon bia");
        can.setUnit(WasteUnit.CAN);
        can.setPointPerUnit(new BigDecimal("180"));

        RecyclableRewardCalculator calculator = new RecyclableRewardCalculator();
        RecyclableRewardCalculator.CalculationResult result = calculator.calculate(List.of(
                new RecyclableRewardCalculator.ItemInput(paper, new BigDecimal("2.0")),
                new RecyclableRewardCalculator.ItemInput(copper, new BigDecimal("0.5")),
                new RecyclableRewardCalculator.ItemInput(can, new BigDecimal("10"))
        ));

        assertEquals(40050, result.totalPoint());
        assertEquals(new BigDecimal("2.5"), result.totalWeightKg());
        assertEquals(3, result.items().size());
    }
}


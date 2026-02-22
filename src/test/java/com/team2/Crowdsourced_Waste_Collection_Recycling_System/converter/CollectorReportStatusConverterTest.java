package com.team2.Crowdsourced_Waste_Collection_Recycling_System.converter;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollectorReportStatusConverterTest {

    private final CollectorReportStatusConverter converter = new CollectorReportStatusConverter();

    @Test
    void convertToEntityAttribute_shouldHandleLowercase() {
        assertEquals(CollectorReportStatus.COMPLETED, converter.convertToEntityAttribute("completed"));
        assertEquals(CollectorReportStatus.COMPLETED, converter.convertToEntityAttribute("Completed"));
        assertEquals(CollectorReportStatus.COMPLETED, converter.convertToEntityAttribute("COMPLETED"));
    }

    @Test
    void convertToEntityAttribute_shouldHandleNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToDatabaseColumn_shouldReturnString() {
        assertEquals("completed", converter.convertToDatabaseColumn(CollectorReportStatus.COMPLETED));
    }

    @Test
    void convertToDatabaseColumn_shouldHandleNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }
}

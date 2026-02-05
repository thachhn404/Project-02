package com.team2.Crowdsourced_Waste_Collection_Recycling_System.converter;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollectorStatusConverterTest {

    private final CollectorStatusConverter converter = new CollectorStatusConverter();

    @Test
    void convertToEntityAttribute_shouldHandleLowercase() {
        assertEquals(CollectorStatus.AVAILABLE, converter.convertToEntityAttribute("available"));
        assertEquals(CollectorStatus.AVAILABLE, converter.convertToEntityAttribute("Available"));
        assertEquals(CollectorStatus.AVAILABLE, converter.convertToEntityAttribute("AVAILABLE"));
    }

    @Test
    void convertToEntityAttribute_shouldHandleNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToDatabaseColumn_shouldReturnString() {
        assertEquals("AVAILABLE", converter.convertToDatabaseColumn(CollectorStatus.AVAILABLE));
    }

    @Test
    void convertToDatabaseColumn_shouldHandleNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }
}

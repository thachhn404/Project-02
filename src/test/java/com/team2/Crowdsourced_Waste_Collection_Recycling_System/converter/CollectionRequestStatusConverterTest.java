package com.team2.Crowdsourced_Waste_Collection_Recycling_System.converter;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollectionRequestStatusConverterTest {

    private final CollectionRequestStatusConverter converter = new CollectionRequestStatusConverter();

    @Test
    void convertToEntityAttribute_shouldHandleLowercase() {
        assertEquals(CollectionRequestStatus.PENDING, converter.convertToEntityAttribute("pending"));
        assertEquals(CollectionRequestStatus.PENDING, converter.convertToEntityAttribute("Pending"));
        assertEquals(CollectionRequestStatus.PENDING, converter.convertToEntityAttribute("PENDING"));
    }

    @Test
    void convertToEntityAttribute_shouldHandleNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToDatabaseColumn_shouldReturnString() {
        assertEquals("PENDING", converter.convertToDatabaseColumn(CollectionRequestStatus.PENDING));
    }

    @Test
    void convertToDatabaseColumn_shouldHandleNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }
}

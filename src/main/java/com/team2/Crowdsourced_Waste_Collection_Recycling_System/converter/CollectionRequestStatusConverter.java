package com.team2.Crowdsourced_Waste_Collection_Recycling_System.converter;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CollectionRequestStatusConverter implements AttributeConverter<CollectionRequestStatus, String> {

    @Override
    public String convertToDatabaseColumn(CollectionRequestStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public CollectionRequestStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return CollectionRequestStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Log warning or handle unknown status
            return null;
        }
    }
}

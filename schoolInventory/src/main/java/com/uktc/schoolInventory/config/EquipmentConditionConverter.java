package com.uktc.schoolInventory.config;

import com.uktc.schoolInventory.models.EquipmentCondition;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EquipmentConditionConverter implements AttributeConverter<EquipmentCondition, String> {

    @Override
    public String convertToDatabaseColumn(EquipmentCondition condition) {
        if (condition == null) return null;
        return switch (condition) {
            case POOR -> "POOR";
            case GOOD -> "GOOD";
            case VERY_GOOD -> "VERY GOOD";
            case EXCELLENT -> "EXCELLENT";
        };
    }

    @Override
    public EquipmentCondition convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return switch (dbValue) {
            case "POOR" -> EquipmentCondition.POOR;
            case "GOOD" -> EquipmentCondition.GOOD;
            case "VERY GOOD" -> EquipmentCondition.VERY_GOOD;
            case "EXCELLENT" -> EquipmentCondition.EXCELLENT;
            default -> throw new IllegalArgumentException("Unknown equipment condition: " + dbValue);
        };
    }
}

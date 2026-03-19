package com.uktc.schoolInventory.config;

import com.uktc.schoolInventory.models.EquipmentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EquipmentStatusConverter implements AttributeConverter<EquipmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(EquipmentStatus status) {
        if (status == null) return null;
        return switch (status) {
            case Available -> "Available";
            case CheckedOut -> "Checked Out";
            case UnderRepair -> "Under Repair";
            case Retired -> "Retired";
        };
    }

    @Override
    public EquipmentStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return switch (dbValue) {
            case "Available" -> EquipmentStatus.Available;
            case "Checked Out" -> EquipmentStatus.CheckedOut;
            case "Under Repair" -> EquipmentStatus.UnderRepair;
            case "Retired" -> EquipmentStatus.Retired;
            default -> throw new IllegalArgumentException("Unknown equipment status: " + dbValue);
        };
    }
}

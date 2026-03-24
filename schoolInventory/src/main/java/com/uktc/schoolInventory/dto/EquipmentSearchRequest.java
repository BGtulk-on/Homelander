package com.uktc.schoolInventory.dto;

import com.uktc.schoolInventory.models.EquipmentCondition;
import com.uktc.schoolInventory.models.EquipmentStatus;
import lombok.Data;

@Data
public class EquipmentSearchRequest {
    private String query;
    private String name;
    private String serialNumber;
    private EquipmentCondition condition;
    private EquipmentStatus status;
    private String typeName;
    private String locationName;
}

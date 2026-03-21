package com.uktc.schoolInventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LowStockAlertResponse {
    private String typeName;
    private long totalCount;
}

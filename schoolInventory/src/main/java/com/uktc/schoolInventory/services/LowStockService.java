package com.uktc.schoolInventory.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.uktc.schoolInventory.dto.response.LowStockAlertResponse;
import com.uktc.schoolInventory.repositories.EquipmentRepository;

@Service
public class LowStockService {

    private final EquipmentRepository equipmentRepository;

    public LowStockService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<LowStockAlertResponse> getLowStockAlerts(int threshold) {
        List<Object[]> totalCounts = equipmentRepository.countTotalGroupedByType();

        List<LowStockAlertResponse> alerts = new ArrayList<>();
        for (Object[] row : totalCounts) {
            String typeName = (String) row[0];
            long total = (Long) row[1];

            if (total <= threshold) {
                alerts.add(new LowStockAlertResponse(typeName, total));
            }
        }

        return alerts;
    }
}

package com.uktc.schoolInventory.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uktc.schoolInventory.dto.response.LowStockAlertResponse;
import com.uktc.schoolInventory.services.LowStockService;

@RestController
@RequestMapping("/api/alerts")
public class LowStockController {

    private final LowStockService lowStockService;

    public LowStockController(LowStockService lowStockService) {
        this.lowStockService = lowStockService;
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockAlerts(
            @RequestParam(defaultValue = "2") int threshold) {
        return ResponseEntity.ok(lowStockService.getLowStockAlerts(threshold));
    }
}

package com.uktc.schoolInventory.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.models.EquipmentSearchRequest;
import com.uktc.schoolInventory.services.EquipmentService;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService service;

    public EquipmentController(EquipmentService service) {
        this.service = service;
    }

    @PostMapping("/all")
    public List<Equipment> getAll() {
        return service.getAllEquipment();
    }

    @PostMapping("/search")
    public List<Equipment> search(@RequestBody EquipmentSearchRequest searchRequest) {
        return service.searchEquipment(
                searchRequest.getName(),
                searchRequest.getCondition(),
                searchRequest.getStatus(),
                searchRequest.getTypeName(),
                searchRequest.getLocationName()
        );
    }

    @PostMapping
    public Equipment create(@RequestBody Equipment equipment) {
        return service.createEquipment(equipment);
    }

    @PutMapping("/{id}")
    public Equipment update(@PathVariable Long id, @RequestBody Equipment details) {
        return service.updateEquipment(id, details);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteEquipment(id);
    }

    @PatchMapping("/{id}/assign")
    public Equipment assign(@PathVariable Long id, @RequestParam String personName) {
        return service.assignEquipment(id, personName);
    }

    @GetMapping("/low-stock")
    public List<Equipment> getLowStock() {
        return service.getLowStockEquipment();
    }
}
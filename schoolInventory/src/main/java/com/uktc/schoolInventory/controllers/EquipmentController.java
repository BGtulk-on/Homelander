package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.services.EquipmentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService service;

    // Инжектираме Service-а вместо Repository-то
    public EquipmentController(EquipmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Equipment> getAll() {
        return service.getAllEquipment();
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
}
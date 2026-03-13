package com.uktc.schoolInventory.controllers; // Важно: този ред казва на Java къде се намира файлът

import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.repositories.EquipmentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentRepository repository;

    public EquipmentController(EquipmentRepository repository) {
        this.repository = repository;
    }
    
    // GET
    @GetMapping
    public List<Equipment> getAll() {
        return repository.findAll();
    }

    // 1. Create
    @PostMapping
    public Equipment create(@RequestBody Equipment equipment) {
        return repository.save(equipment);
    }

    // 2. Update
    @PutMapping("/{id}")
    public Equipment update(@PathVariable Long id, @RequestBody Equipment details) {
        Equipment equipment = repository.findById(id).orElseThrow();
        equipment.setName(details.getName());
        equipment.setType(details.getType());
        equipment.setSerialNumber(details.getSerialNumber());
        return repository.save(equipment);
    }

    // 3. Delete
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // 4. Assign
    @PatchMapping("/{id}/assign")
    public Equipment assign(@PathVariable Long id, @RequestParam String personName) {
        Equipment equipment = repository.findById(id).orElseThrow();
        equipment.setAssigned(true);
        equipment.setAssignedTo(personName);
        return repository.save(equipment);
    }
}
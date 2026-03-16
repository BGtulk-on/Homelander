package com.uktc.schoolInventory.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.repositories.EquipmentRepository;

@Service
public class EquipmentService {

    private final EquipmentRepository repository;

    public EquipmentService(EquipmentRepository repository) {
        this.repository = repository;
    }

    public List<Equipment> getAllEquipment() {
        return repository.findAll();
    }

    public Equipment createEquipment(Equipment equipment) {
        return repository.save(equipment);
    }

    public Equipment updateEquipment(Long id, Equipment details) {
        Equipment equipment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));

        equipment.setName(details.getName());
        equipment.setTypeId(details.getTypeId());
        equipment.setSerialNumber(details.getSerialNumber());
        equipment.setPhotoUrl(details.getPhotoUrl());
        equipment.setCurrentCondition(details.getCurrentCondition());

        return repository.save(equipment);
    }

    public void deleteEquipment(Long id) {
        repository.deleteById(id);
    }

    public Equipment assignEquipment(Long id, String personName) {
        Equipment equipment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        equipment.setAssigned(true);
        equipment.setAssignedTo(personName);

        return repository.save(equipment);
    }
}
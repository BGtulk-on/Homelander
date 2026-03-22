package com.uktc.schoolInventory.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.models.EquipmentCondition;
import com.uktc.schoolInventory.models.EquipmentStatus;
import com.uktc.schoolInventory.repositories.EquipmentRepository;
import com.uktc.schoolInventory.specifications.EquipmentSpecification;

@Service
public class EquipmentService {

    private final EquipmentRepository repository;

    @Value("${inventory.low-stock.threshold}")
    private int lowStockThreshold;

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
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));

        equipment.setName(details.getName());
        equipment.setTypeId(details.getTypeId());
        equipment.setSerialNumber(details.getSerialNumber());
        equipment.setPhotoUrl(details.getPhotoUrl());
        equipment.setCurrentCondition(details.getCurrentCondition());
        equipment.setStatus(details.getStatus());
        equipment.setLocationId(details.getLocationId());
        equipment.setAssignedTo(details.getAssignedTo());
        equipment.setAssigned(details.getAssignedTo() != null && !details.getAssignedTo().isEmpty());

        return repository.save(equipment);
    }

    public void deleteEquipment(Long id) {
        repository.deleteById(id);
    }

    public Equipment assignEquipment(Long id, String personName) {
        Equipment equipment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));

        equipment.setAssigned(true);
        equipment.setAssignedTo(personName);

        return repository.save(equipment);
    }

    public List<Equipment> searchEquipment(String query, String name, String serialNumber, EquipmentCondition condition, EquipmentStatus status, String typeName, String locationName) {
        Specification<Equipment> spec = (root, q, cb) -> cb.conjunction();

        if (query != null && !query.isBlank()) {
            spec = spec.and(EquipmentSpecification.hasQuery(query));
        }
        if (name != null && !name.isBlank()) {
            spec = spec.and(EquipmentSpecification.hasNameLike(name));
        }
        if (serialNumber != null && !serialNumber.isBlank()) {
            spec = spec.and(EquipmentSpecification.hasSerialNumberLike(serialNumber));
        }
        if (condition != null) {
            spec = spec.and(EquipmentSpecification.hasCondition(condition));
        }
        if (status != null) {
            spec = spec.and(EquipmentSpecification.hasStatus(status));
        }
        if (typeName != null && !typeName.isBlank()) {
            spec = spec.and(EquipmentSpecification.hasTypeName(typeName));
        }
        if (locationName != null && !locationName.isBlank()) {
            spec = spec.and(EquipmentSpecification.hasLocationName(locationName));
        }

        return repository.findAll(spec);
    }

    public List<Equipment> getLowStockEquipment() {
        return repository.findLowStockEquipment(lowStockThreshold);
    }
}
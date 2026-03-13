package com.uktc.schoolInventory.repositories;

import com.uktc.schoolInventory.models.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    // Тук автоматично имаме save(), findById(), deleteById() и т.н.
}
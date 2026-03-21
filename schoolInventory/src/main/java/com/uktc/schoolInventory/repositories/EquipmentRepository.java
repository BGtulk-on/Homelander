package com.uktc.schoolInventory.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.uktc.schoolInventory.models.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    @Query("SELECT t.typeName, COUNT(e) FROM Equipment e JOIN e.type t GROUP BY t.typeName")
    List<Object[]> countTotalGroupedByType();
}
package com.uktc.schoolInventory.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uktc.schoolInventory.models.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    @Query("SELECT e FROM Equipment e JOIN e.type t WHERE t.typeName IN " +
           "(SELECT t2.typeName FROM Equipment e2 JOIN e2.type t2 GROUP BY t2.typeName HAVING COUNT(e2) <= :threshold)")
    List<Equipment> findLowStockEquipment(@Param("threshold") int threshold);
}
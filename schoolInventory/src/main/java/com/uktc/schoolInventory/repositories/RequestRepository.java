package com.uktc.schoolInventory.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequestStatus(RequestStatusType status);

    List<Request> findByUserIdAndRequestStatus(Long userId, RequestStatusType status);

    List<Request> findByEquipmentIdAndRequestStatus(Long equipmentId, RequestStatusType status);

    // Намираме всички заявки на даден потребител
    List<Request> findAllByUser_Id(Long userId);

    List<Request> findAllByEquipment_Id(Long equipmentId);
}
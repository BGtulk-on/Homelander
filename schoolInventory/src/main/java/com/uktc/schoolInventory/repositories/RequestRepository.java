package com.uktc.schoolInventory.repositories;

import com.uktc.schoolInventory.models.RequestStatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.uktc.schoolInventory.models.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    // Намираме всички одобрени заявки
    List<Request> findAllByRequestStatus(RequestStatusType status);
}
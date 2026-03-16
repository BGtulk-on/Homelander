package com.uktc.schoolInventory.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uktc.schoolInventory.models.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
}
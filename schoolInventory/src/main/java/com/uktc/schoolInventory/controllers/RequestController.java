package com.uktc.schoolInventory.controllers;

import java.net.URI;
import java.time.OffsetDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.EquipmentCondition;
import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.repositories.RequestRepository;
import com.uktc.schoolInventory.services.RequestService;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestService requestService;
    private final RequestRepository repository;

    public RequestController(RequestService requestService, RequestRepository repository) {
        this.requestService = requestService;
        this.repository = repository;
    }

    @GetMapping("/user/{userId}")
    public List<Request> getRequestsByUser(@PathVariable Long userId) {
        return repository.findAllByUser_Id(userId);
    }

    @GetMapping("/all")
    public List<Request> getAllRequests() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Request> createRequest(@RequestBody Request request) {
        // Извикваме метода в Service, който праща имейл
        Request saved = requestService.createRequest(request);
        return ResponseEntity
                .created(URI.create("/api/requests/" + saved.getId()))
                .body(saved);
    }
    @PutMapping("/{id}/approve")
    public ResponseEntity<Request> approveRequest(@PathVariable Long id, @RequestParam Long adminId) {
        Request request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        request.setRequestStatus(RequestStatusType.APPROVED);
        request.setApprovedByAdminId(adminId);
        return ResponseEntity.ok(repository.save(request));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Request> rejectRequest(@PathVariable Long id, @RequestParam Long adminId) {
        Request request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        request.setRequestStatus(RequestStatusType.REJECTED);
        request.setApprovedByAdminId(adminId);
        return ResponseEntity.ok(repository.save(request));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Request> returnEquipment(@PathVariable Long id, @RequestParam EquipmentCondition condition) {
        Request request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        request.setRequestStatus(RequestStatusType.RETURNED);
        request.setActualReturnDate(OffsetDateTime.now());
        request.setReturnCondition(condition);
        return ResponseEntity.ok(repository.save(request));
    }
}

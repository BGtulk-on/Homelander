package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.models.EquipmentCondition;
import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.repositories.RequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestRepository repository;

    public RequestController(RequestRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Request> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Request> createRequest(@RequestBody Request request) {
        request.setRequestStatus(RequestStatusType.PENDING);
        Request saved = repository.save(request);
        return ResponseEntity
                .created(URI.create("/api/requests/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Request> approveRequest(@PathVariable Long id, @RequestParam Long adminId) {
        return repository.findById(id)
                .map(request -> {
                    request.setRequestStatus(RequestStatusType.APPROVED);
                    request.setApprovedByAdminId(adminId);
                    return ResponseEntity.ok(repository.save(request));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Request> rejectRequest(@PathVariable Long id, @RequestParam Long adminId) {
        return repository.findById(id)
                .map(request -> {
                    request.setRequestStatus(RequestStatusType.REJECTED);
                    request.setApprovedByAdminId(adminId);
                    return ResponseEntity.ok(repository.save(request));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Request> returnEquipment(@PathVariable Long id, @RequestParam EquipmentCondition condition) {
        return repository.findById(id)
                .map(request -> {
                    request.setRequestStatus(RequestStatusType.RETURNED);
                    request.setActualReturnDate(OffsetDateTime.now());
                    request.setReturnCondition(condition);
                    return ResponseEntity.ok(repository.save(request));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
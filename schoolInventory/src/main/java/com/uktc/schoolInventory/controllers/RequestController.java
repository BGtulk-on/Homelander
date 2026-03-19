package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.models.EquipmentCondition;
import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.repositories.RequestRepository;
import com.uktc.schoolInventory.services.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestService requestService;
    private final RequestRepository repository;

    public RequestController(RequestService requestService, RequestRepository repository) {
        this.requestService = requestService;
        this.repository = repository;
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
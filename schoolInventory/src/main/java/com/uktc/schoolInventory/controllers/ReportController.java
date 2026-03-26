package com.uktc.schoolInventory.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.uktc.schoolInventory.controllers.user.Role;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import com.uktc.schoolInventory.services.ReportService;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    /**
     * Any user can export their own report.
     * GET /reports/my/export?userId=2&format=csv
     */
    @GetMapping("/my/export")
    public ResponseEntity<?> exportMyReport(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "csv") String format) {
        try {
            return buildUserFileResponse(userId, format, "my_report");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Export failed: " + e.getMessage());
        }
    }

    /**
     * Admin-only: export any user's report.
     * GET /reports/user/2/export?requestingUserId=1&format=pdf
     */
    @GetMapping("/user/{userId}/export")
    public ResponseEntity<?> exportUserReport(
            @PathVariable Long userId,
            @RequestParam Long requestingUserId,
            @RequestParam(defaultValue = "csv") String format) {
        if (!isAdmin(requestingUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can access other users' reports");
        }
        try {
            return buildUserFileResponse(userId, format, "user_" + userId + "_report");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Export failed: " + e.getMessage());
        }
    }

    /**
     * Admin-only: export all equipment report.
     * GET /reports/equipment/all/export?requestingUserId=1&format=csv
     */
    @GetMapping("/equipment/all/export")
    public ResponseEntity<?> exportAllEquipmentReport(
            @RequestParam Long requestingUserId,
            @RequestParam(defaultValue = "csv") String format) {
        if (!isAdmin(requestingUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can access equipment reports");
        }
        try {
            return buildAllEquipmentFileResponse(format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Export failed: " + e.getMessage());
        }
    }

    /**
     * Admin-only: export all requests report.
     * GET /reports/requests/all/export?requestingUserId=1&format=pdf
     */
    @GetMapping("/requests/all/export")
    public ResponseEntity<?> exportAllRequestsReport(
            @RequestParam Long requestingUserId,
            @RequestParam(defaultValue = "csv") String format) {
        if (!isAdmin(requestingUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can access request reports");
        }
        try {
            return buildAllRequestsFileResponse(format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Export failed: " + e.getMessage());
        }
    }

    /**
     * Admin-only: export a specific equipment's report.
     * GET /reports/equipment/3/export?requestingUserId=1&format=csv
     */
    @GetMapping("/equipment/{equipmentId}/export")
    public ResponseEntity<?> exportEquipmentReport(
            @PathVariable Long equipmentId,
            @RequestParam Long requestingUserId,
            @RequestParam(defaultValue = "csv") String format) {
        if (!isAdmin(requestingUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can access equipment reports");
        }
        try {
            return buildEquipmentFileResponse(equipmentId, format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Export failed: " + e.getMessage());
        }
    }

    // ==================== Helpers ====================

    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == Role.ADMIN || user.getRole() == Role.SUPERUSER)
                .orElse(false);
    }

    private ResponseEntity<byte[]> buildUserFileResponse(Long userId, String format, String prefix) throws Exception {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = String.format("%s_%s.%s", prefix, timestamp, format);

        byte[] content;
        MediaType mediaType;
        if ("pdf".equalsIgnoreCase(format)) {
            content = reportService.exportPdfForUser(userId);
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            content = reportService.exportCsvForUser(userId);
            mediaType = MediaType.parseMediaType("text/csv");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .contentLength(content.length)
                .body(content);
    }

    private ResponseEntity<byte[]> buildAllEquipmentFileResponse(String format) throws Exception {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = String.format("all_equipment_%s.%s", timestamp, format);

        byte[] content;
        MediaType mediaType;
        if ("pdf".equalsIgnoreCase(format)) {
            content = reportService.exportPdfForAllEquipment();
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            content = reportService.exportCsvForAllEquipment();
            mediaType = MediaType.parseMediaType("text/csv");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .contentLength(content.length)
                .body(content);
    }

    private ResponseEntity<byte[]> buildAllRequestsFileResponse(String format) throws Exception {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = String.format("all_requests_%s.%s", timestamp, format);

        byte[] content;
        MediaType mediaType;
        if ("pdf".equalsIgnoreCase(format)) {
            content = reportService.exportPdfForAllRequests();
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            content = reportService.exportCsvForAllRequests();
            mediaType = MediaType.parseMediaType("text/csv");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .contentLength(content.length)
                .body(content);
    }

    private ResponseEntity<byte[]> buildEquipmentFileResponse(Long equipmentId, String format) throws Exception {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = String.format("equipment_%d_report_%s.%s", equipmentId, timestamp, format);

        byte[] content;
        MediaType mediaType;
        if ("pdf".equalsIgnoreCase(format)) {
            content = reportService.exportPdfForEquipment(equipmentId);
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            content = reportService.exportCsvForEquipment(equipmentId);
            mediaType = MediaType.parseMediaType("text/csv");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .contentLength(content.length)
                .body(content);
    }
}

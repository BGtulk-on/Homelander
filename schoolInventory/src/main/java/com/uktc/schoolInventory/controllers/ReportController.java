package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.services.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private static final String REPORTS_DIR = "reports";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getUsage() {
        return ResponseEntity.ok(reportService.getUsageReport());
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory() {
        return ResponseEntity.ok(reportService.getHistoryReport());
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, String>> export(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(defaultValue = "user") String type) {

        try {
            String reportUrl = generateReport(type, format);
            return ResponseEntity.ok(Map.of("reportUrl", reportUrl));
        } catch (Exception e) {
            throw new RuntimeException("Export failed: " + e.getMessage());
        }
    }

    @GetMapping("/user/export")
    public ResponseEntity<Map<String, String>> exportUserReport(
            @RequestParam(defaultValue = "csv") String format) {

        try {
            String reportUrl = generateReport("user", format);
            return ResponseEntity.ok(Map.of("reportUrl", reportUrl));
        } catch (Exception e) {
            throw new RuntimeException("User report export failed: " + e.getMessage());
        }
    }

    @GetMapping("/equipment/export")
    public ResponseEntity<Map<String, String>> exportEquipmentReport(
            @RequestParam(defaultValue = "csv") String format) {

        try {
            String reportUrl = generateReport("equipment", format);
            return ResponseEntity.ok(Map.of("reportUrl", reportUrl));
        } catch (Exception e) {
            throw new RuntimeException("Equipment report export failed: " + e.getMessage());
        }
    }

    // Factory pattern logic moved to controller
    private String generateReport(String type, String format) throws IOException, com.lowagie.text.DocumentException {
        Path reportsDir = Paths.get(REPORTS_DIR);
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = String.format("%s_report_%s.%s", type, timestamp, format);
        Path filePath = reportsDir.resolve(filename);

        byte[] content;
        if ("pdf".equalsIgnoreCase(format)) {
            content = reportService.exportPdf(type);
        } else {
            content = reportService.exportCsv(type);
        }

        Files.write(filePath, content);
        return "/reports/" + filename;
    }
}

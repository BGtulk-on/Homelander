package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.dto.ReportRowDto;
import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.Report;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.repositories.ReportRepository;
import com.uktc.schoolInventory.repositories.RequestRepository;
import com.uktc.schoolInventory.repositories.UserRepository;
import com.uktc.schoolInventory.repositories.EquipmentRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportService {

    private final RequestRepository requestRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReportService(RequestRepository requestRepository, ReportRepository reportRepository,
                        UserRepository userRepository, EquipmentRepository equipmentRepository) {
        this.requestRepository = requestRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public void syncReportFromRequest(Request req) {
        if (req.getRequestStatus() != RequestStatusType.RETURNED) return;
        if (reportRepository.findAll().stream().anyMatch(r -> r.getRequest().getId().equals(req.getId()))) return;

        Report report = new Report();
        report.setEquipment(req.getEquipment());
        report.setUser(req.getUser());
        report.setRequest(req);
        reportRepository.save(report);
    }

    public Map<String, Object> getUsageReport() {
        List<Request> all = requestRepository.findAllReturnedForReports();
        long totalReturns = all.size();
        long uniqueUsers = all.stream().map(r -> r.getUser().getId()).distinct().count();
        long uniqueEquipment = all.stream().map(r -> r.getEquipment().getId()).distinct().count();

        return Map.of(
            "totalReturnedRequests", totalReturns,
            "uniqueUsers", uniqueUsers,
            "uniqueEquipment", uniqueEquipment
        );
    }

    public Map<String, Object> getHistoryReport() {
        List<ReportRowDto> rows = buildReportRowsFromRequests(requestRepository.findAllReturnedForReports());
        return Map.of("history", rows);
    }

    public List<ReportRowDto> getUserReport() {
        List<Request> returned = requestRepository.findAllReturnedForReports();
        return buildReportRowsFromRequests(returned);
    }

    public List<ReportRowDto> getEquipmentReport() {
        List<Request> returned = requestRepository.findAllReturnedForReports();
        return buildReportRowsFromRequests(returned);
    }

    private List<ReportRowDto> buildReportRowsFromRequests(List<Request> requests) {
        List<ReportRowDto> rows = new ArrayList<>();
        for (Request r : requests) {
            User user = r.getUser();
            Equipment equip = r.getEquipment();
            
            if (user != null && equip != null) {
                String userName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "").trim();
                String userEmail = user.getEmail();
                String eqName = equip.getName();
                String eqType = equip.getType() != null ? equip.getType().getTypeName() : (equip.getTypeId() != null ? "Type " + equip.getTypeId() : "");
                String serial = equip.getSerialNumber() != null ? equip.getSerialNumber() : "";
                
                rows.add(ReportRowDto.forUserReport(userName, userEmail, eqName, eqType, serial,
                        convertToLocalDateTime(r.getCreatedAt()), convertToLocalDateTime(r.getRequestedEndDate()), 
                        convertToLocalDateTime(r.getActualReturnDate()), 
                        r.getReturnCondition() != null ? r.getReturnCondition().toString() : ""));
            }
        }
        return rows;
    }

    public byte[] exportCsv(String type) throws IOException {
        List<ReportRowDto> rows = "equipment".equalsIgnoreCase(type) ? getEquipmentReport() : getUserReport();
        String[] headers = type.equalsIgnoreCase("equipment")
                ? new String[]{"Equipment Name", "Type", "Serial", "Requested By", "User Email", "Requested At", "Return Deadline", "Actual Return", "Condition"}
                : new String[]{"User Name", "User Email", "Equipment", "Type", "Serial", "Requested At", "Return Deadline", "Actual Return", "Condition"};

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(headers))) {
            for (ReportRowDto row : rows) {
                if ("equipment".equalsIgnoreCase(type)) {
                    printer.printRecord(
                            row.getEquipmentName(), row.getEquipmentType(), row.getSerialNumber(),
                            row.getUserName(), row.getUserEmail(),
                            fmt(row.getRequestedAt()), fmt(row.getReturnDeadline()), fmt(row.getActualReturnDate()),
                            row.getConditionOnReturn());
                } else {
                    printer.printRecord(
                            row.getUserName(), row.getUserEmail(),
                            row.getEquipmentName(), row.getEquipmentType(), row.getSerialNumber(),
                            fmt(row.getRequestedAt()), fmt(row.getReturnDeadline()), fmt(row.getActualReturnDate()),
                            row.getConditionOnReturn());
                }
            }
        }
        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportPdf(String type) throws DocumentException, IOException {
        List<ReportRowDto> rows = "equipment".equalsIgnoreCase(type) ? getEquipmentReport() : getUserReport();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        document.add(new Paragraph((type.equalsIgnoreCase("equipment") ? "Equipment" : "User") + " Report", titleFont));
        document.add(new Paragraph(" "));

        PdfPTable table;
        if ("equipment".equalsIgnoreCase(type)) {
            table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.addCell("Equipment");
            table.addCell("Type");
            table.addCell("Serial");
            table.addCell("Requested By");
            table.addCell("Email");
            table.addCell("Requested At");
            table.addCell("Deadline");
            table.addCell("Actual Return");
            table.addCell("Condition");
        } else {
            table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.addCell("User");
            table.addCell("Email");
            table.addCell("Equipment");
            table.addCell("Type");
            table.addCell("Serial");
            table.addCell("Requested At");
            table.addCell("Deadline");
            table.addCell("Actual Return");
            table.addCell("Condition");
        }

        for (ReportRowDto row : rows) {
            addCell(table, type.equalsIgnoreCase("equipment") ? row.getEquipmentName() : row.getUserName());
            addCell(table, type.equalsIgnoreCase("equipment") ? row.getEquipmentType() : row.getUserEmail());
            addCell(table, type.equalsIgnoreCase("equipment") ? row.getSerialNumber() : row.getEquipmentName());
            addCell(table, type.equalsIgnoreCase("equipment") ? row.getUserName() : row.getEquipmentType());
            addCell(table, type.equalsIgnoreCase("equipment") ? row.getUserEmail() : row.getSerialNumber());
            addCell(table, fmt(row.getRequestedAt()));
            addCell(table, fmt(row.getReturnDeadline()));
            addCell(table, fmt(row.getActualReturnDate()));
            addCell(table, row.getConditionOnReturn() != null ? row.getConditionOnReturn() : "");
        }
        document.add(table);
        document.close();
        return out.toByteArray();
    }

    private static void addCell(PdfPTable table, String text) {
        table.addCell(new PdfPCell(new Phrase(text != null ? text : "")));
    }

    private static String fmt(java.time.LocalDateTime dt) {
        return dt != null ? dt.format(FORMAT) : "";
    }

    private static LocalDateTime convertToLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}

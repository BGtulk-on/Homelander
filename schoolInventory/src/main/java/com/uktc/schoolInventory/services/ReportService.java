package com.uktc.schoolInventory.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.uktc.schoolInventory.dto.ReportRowDto;
import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.RequestRepository;

@Service
public class ReportService {

    private final RequestRepository requestRepository;
    private final com.uktc.schoolInventory.repositories.EquipmentRepository equipmentRepository;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReportService(RequestRepository requestRepository, com.uktc.schoolInventory.repositories.EquipmentRepository equipmentRepository) {
        this.requestRepository = requestRepository;
        this.equipmentRepository = equipmentRepository;
    }

    // ==================== All Equipment Reports ====================

    @Transactional(readOnly = true)
    public byte[] exportCsvForAllEquipment() throws IOException {
        List<Equipment> items = equipmentRepository.findAll();
        String[] headers = { "Name", "Type", "Serial", "Location", "Status", "Condition", "Assigned To" };

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
            for (Equipment e : items) {
                printer.printRecord(
                    e.getName(), 
                    e.getType() != null ? e.getType().getTypeName() : "N/A",
                    e.getSerialNumber(),
                    e.getLocation() != null ? e.getLocation().getRoomName() : "N/A",
                    e.getStatus(),
                    e.getCurrentCondition(),
                    e.getAssignedTo()
                );
            }
        }
        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportPdfForAllEquipment() throws DocumentException {
        List<Equipment> items = equipmentRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4.rotate())) {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("All Equipment Inventory Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.addCell("Name"); table.addCell("Type"); table.addCell("Serial"); table.addCell("Location");
            table.addCell("Status"); table.addCell("Condition"); table.addCell("Assigned");
            for (Equipment e : items) {
                addCell(table, e.getName());
                addCell(table, e.getType() != null ? e.getType().getTypeName() : "");
                addCell(table, e.getSerialNumber());
                addCell(table, e.getLocation() != null ? e.getLocation().getRoomName() : "");
                addCell(table, e.getStatus() != null ? e.getStatus().toString() : "");
                addCell(table, e.getCurrentCondition() != null ? e.getCurrentCondition().toString() : "");
                addCell(table, e.getAssignedTo());
            }
            document.add(table);
        }
        return out.toByteArray();
    }

    // ==================== All Requests Reports ====================

    @Transactional(readOnly = true)
    public byte[] exportCsvForAllRequests() throws IOException {
        List<Request> items = requestRepository.findAll();
        String[] headers = { "User", "Equipment", "Status", "Requested At", "Deadline", "Returned At" };

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
            for (Request r : items) {
                printer.printRecord(
                    r.getUser() != null ? r.getUser().getEmail() : "N/A",
                    r.getEquipment() != null ? r.getEquipment().getName() : "N/A",
                    r.getRequestStatus(),
                    fmt(convertToLocalDateTime(r.getRequestedStartDate())),
                    fmt(convertToLocalDateTime(r.getRequestedEndDate())),
                    fmt(convertToLocalDateTime(r.getActualReturnDate()))
                );
            }
        }
        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportPdfForAllRequests() throws DocumentException {
        List<Request> items = requestRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4.rotate())) {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("All Equipment Requests Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("User"); table.addCell("Equipment"); table.addCell("Status");
            table.addCell("Requested At"); table.addCell("Deadline"); table.addCell("Returned At");
            for (Request r : items) {
                addCell(table, r.getUser() != null ? r.getUser().getEmail() : "");
                addCell(table, r.getEquipment() != null ? r.getEquipment().getName() : "");
                addCell(table, r.getRequestStatus() != null ? r.getRequestStatus().toString() : "");
                addCell(table, fmt(convertToLocalDateTime(r.getRequestedStartDate())));
                addCell(table, fmt(convertToLocalDateTime(r.getRequestedEndDate())));
                addCell(table, fmt(convertToLocalDateTime(r.getActualReturnDate())));
            }
            document.add(table);
        }
        return out.toByteArray();
    }

    // ==================== Per-User Reports ====================

    @Transactional(readOnly = true)
    public byte[] exportCsvForUser(Long userId) throws IOException {
        List<ReportRowDto> rows = getReportForUser(userId);

        String[] headers = { "Equipment", "Type", "Serial", "Requested At", "Return Deadline", "Actual Return",
                "Condition" };

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
            for (ReportRowDto row : rows) {
                printer.printRecord(
                        row.getEquipmentName(), row.getEquipmentType(), row.getSerialNumber(),
                        fmt(row.getRequestedAt()), fmt(row.getReturnDeadline()), fmt(row.getActualReturnDate()),
                        row.getConditionOnReturn());
            }
        }
        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportPdfForUser(Long userId) throws DocumentException {
        List<ReportRowDto> rows = getReportForUser(userId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4.rotate())) {
            PdfWriter.getInstance(document, out);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("User Report", titleFont));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.addCell("Equipment");
            table.addCell("Type");
            table.addCell("Serial");
            table.addCell("Requested At");
            table.addCell("Deadline");
            table.addCell("Actual Return");
            table.addCell("Condition");
            for (ReportRowDto row : rows) {
                addCell(table, row.getEquipmentName());
                addCell(table, row.getEquipmentType());
                addCell(table, row.getSerialNumber());
                addCell(table, fmt(row.getRequestedAt()));
                addCell(table, fmt(row.getReturnDeadline()));
                addCell(table, fmt(row.getActualReturnDate()));
                addCell(table, row.getConditionOnReturn() != null ? row.getConditionOnReturn() : "");
            }
            document.add(table);
        }
        return out.toByteArray();
    }

    // ==================== Per-Equipment Reports ====================

    @Transactional(readOnly = true)
    public byte[] exportCsvForEquipment(Long equipmentId) throws IOException {
        List<ReportRowDto> rows = getReportForEquipment(equipmentId);
        String[] headers = { "Requested By", "Email", "Requested At", "Return Deadline", "Actual Return", "Condition" };

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
            for (ReportRowDto row : rows) {
                printer.printRecord(
                        row.getUserName(), row.getUserEmail(),
                        fmt(row.getRequestedAt()), fmt(row.getReturnDeadline()), fmt(row.getActualReturnDate()),
                        row.getConditionOnReturn());
            }
        }
        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportPdfForEquipment(Long equipmentId) throws DocumentException {
        List<ReportRowDto> rows = getReportForEquipment(equipmentId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4.rotate())) {
            PdfWriter.getInstance(document, out);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("Equipment Report", titleFont));
            document.add(new Paragraph(" "));
            if (rows.isEmpty()) {
                Font noDataFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12);
                document.add(new Paragraph("No data available for this equipment.", noDataFont));
            } else {
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.addCell("Requested By");
                table.addCell("Email");
                table.addCell("Requested At");
                table.addCell("Deadline");
                table.addCell("Actual Return");
                table.addCell("Condition");
                for (ReportRowDto row : rows) {
                    addCell(table, row.getUserName());
                    addCell(table, row.getUserEmail());
                    addCell(table, fmt(row.getRequestedAt()));
                    addCell(table, fmt(row.getReturnDeadline()));
                    addCell(table, fmt(row.getActualReturnDate()));
                    addCell(table, row.getConditionOnReturn() != null ? row.getConditionOnReturn() : "");
                }
                document.add(table);
            }
        }
        return out.toByteArray();
    }

    // ==================== Helpers ====================

    private List<ReportRowDto> getReportForUser(Long userId) {
        List<Request> allUserRequests = requestRepository.findAllByUser_Id(userId);
        return buildReportRows(allUserRequests);
    }

    private List<ReportRowDto> getReportForEquipment(Long equipmentId) {
        List<Request> allEquipmentRequests = requestRepository.findAllByEquipment_Id(equipmentId);
        return buildReportRows(allEquipmentRequests);
    }

    private List<ReportRowDto> buildReportRows(List<Request> requests) {
        List<ReportRowDto> rows = new ArrayList<>();
        for (Request r : requests) {
            User user = r.getUser();
            Equipment equip = r.getEquipment();

            if (user != null && equip != null) {
                String userName = user.getFirstName() + " "
                        + (user.getLastName() != null ? user.getLastName() : "").trim();
                String userEmail = user.getEmail();
                String eqName = equip.getName();
                String eqType = equip.getType() != null ? equip.getType().getTypeName()
                        : (equip.getTypeId() != null ? "Type " + equip.getTypeId() : "");
                String serial = equip.getSerialNumber() != null ? equip.getSerialNumber() : "";

                rows.add(ReportRowDto.forUserReport(userName, userEmail, eqName, eqType, serial,
                        convertToLocalDateTime(r.getRequestedStartDate()),
                        convertToLocalDateTime(r.getRequestedEndDate()),
                        convertToLocalDateTime(r.getActualReturnDate()),
                        r.getReturnCondition() != null ? r.getReturnCondition().toString() : ""));
            }
        }
        return rows;
    }

    // Removed unused method resolveEquipmentName

    private static void addCell(PdfPTable table, String text) {
        table.addCell(new PdfPCell(new Phrase(text != null ? text : "")));
    }

    private static String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(FORMAT) : "";
    }

    private static LocalDateTime convertToLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}

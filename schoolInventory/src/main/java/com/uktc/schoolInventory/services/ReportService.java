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

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReportService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
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

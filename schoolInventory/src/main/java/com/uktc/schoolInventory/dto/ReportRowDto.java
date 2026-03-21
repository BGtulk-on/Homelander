package com.uktc.schoolInventory.dto;

import java.time.LocalDateTime;

/**
 * A single row for user report or equipment report.
 */
public class ReportRowDto {
    private String userName;
    private String userEmail;
    private String equipmentName;
    private String equipmentType;
    private String serialNumber;
    private LocalDateTime requestedAt;
    private LocalDateTime returnDeadline;
    private LocalDateTime actualReturnDate;
    private String conditionOnReturn;

    public static ReportRowDto forUserReport(String userName, String userEmail, String equipmentName,
            String equipmentType, String serialNumber, LocalDateTime requestedAt, LocalDateTime returnDeadline,
            LocalDateTime actualReturnDate, String conditionOnReturn) {
        ReportRowDto dto = new ReportRowDto();
        dto.userName = userName;
        dto.userEmail = userEmail;
        dto.equipmentName = equipmentName;
        dto.equipmentType = equipmentType;
        dto.serialNumber = serialNumber;
        dto.requestedAt = requestedAt;
        dto.returnDeadline = returnDeadline;
        dto.actualReturnDate = actualReturnDate;
        dto.conditionOnReturn = conditionOnReturn;
        return dto;
    }

    public static ReportRowDto forEquipmentReport(String equipmentName, String equipmentType, String serialNumber,
            String userName, String userEmail, LocalDateTime requestedAt, LocalDateTime returnDeadline,
            LocalDateTime actualReturnDate, String conditionOnReturn) {
        ReportRowDto dto = new ReportRowDto();
        dto.equipmentName = equipmentName;
        dto.equipmentType = equipmentType;
        dto.serialNumber = serialNumber;
        dto.userName = userName;
        dto.userEmail = userEmail;
        dto.requestedAt = requestedAt;
        dto.returnDeadline = returnDeadline;
        dto.actualReturnDate = actualReturnDate;
        dto.conditionOnReturn = conditionOnReturn;
        return dto;
    }

    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getEquipmentName() { return equipmentName; }
    public String getEquipmentType() { return equipmentType; }
    public String getSerialNumber() { return serialNumber; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getReturnDeadline() { return returnDeadline; }
    public LocalDateTime getActualReturnDate() { return actualReturnDate; }
    public String getConditionOnReturn() { return conditionOnReturn; }
}

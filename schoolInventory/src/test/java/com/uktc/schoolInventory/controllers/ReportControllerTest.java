package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.exception.GlobalExceptionHandler;
import com.uktc.schoolInventory.services.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                "admin@example.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Authentication superuserAuth() {
        return new UsernamePasswordAuthenticationToken(
                "super@example.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERUSER")));
    }

    private Authentication userAuth() {
        return new UsernamePasswordAuthenticationToken(
                "user@example.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // ==================== /my/export ====================

    @Test
    void exportMyReport_csv_returnsFile() throws Exception {
        when(reportService.exportCsvForUser(1L)).thenReturn("csv-data".getBytes());

        mockMvc.perform(get("/reports/my/export").param("userId", "1").param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("my_report")));
    }

    @Test
    void exportMyReport_pdf_returnsFile() throws Exception {
        when(reportService.exportPdfForUser(1L)).thenReturn("pdf-data".getBytes());

        mockMvc.perform(get("/reports/my/export").param("userId", "1").param("format", "pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".pdf")));
    }

    // ==================== /user/{id}/export ====================

    @Test
    void exportUserReport_admin_returnsOk() throws Exception {
        when(reportService.exportCsvForUser(2L)).thenReturn("csv-data".getBytes());

        mockMvc.perform(get("/reports/user/2/export")
                        .principal(adminAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void exportUserReport_superuser_returnsOk() throws Exception {
        when(reportService.exportCsvForUser(2L)).thenReturn("csv-data".getBytes());

        mockMvc.perform(get("/reports/user/2/export")
                        .principal(superuserAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void exportUserReport_regularUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/reports/user/2/export")
                        .principal(userAuth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportUserReport_noAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get("/reports/user/2/export"))
                .andExpect(status().isForbidden());
    }

    // ==================== /equipment/all/export ====================

    @Test
    void exportAllEquipment_admin_returnsOk() throws Exception {
        when(reportService.exportCsvForAllEquipment()).thenReturn("csv-data".getBytes());

        mockMvc.perform(get("/reports/equipment/all/export")
                        .principal(adminAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void exportAllEquipment_regularUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/reports/equipment/all/export")
                        .principal(userAuth()))
                .andExpect(status().isForbidden());
    }

    // ==================== /requests/all/export ====================

    @Test
    void exportAllRequests_admin_returnsOk() throws Exception {
        when(reportService.exportCsvForAllRequests()).thenReturn("csv-data".getBytes());

        mockMvc.perform(get("/reports/requests/all/export")
                        .principal(adminAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void exportAllRequests_regularUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/reports/requests/all/export")
                        .principal(userAuth()))
                .andExpect(status().isForbidden());
    }

    // ==================== /equipment/{id}/export ====================

    @Test
    void exportEquipmentReport_admin_returnsOk() throws Exception {
        when(reportService.exportCsvForEquipment(3L)).thenReturn("csv-data".getBytes());

        mockMvc.perform(get("/reports/equipment/3/export")
                        .principal(adminAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void exportEquipmentReport_regularUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/reports/equipment/3/export")
                        .principal(userAuth()))
                .andExpect(status().isForbidden());
    }

    // ==================== Error handling ====================

    @Test
    void exportMyReport_serviceThrows_returnsErrorMessage() throws Exception {
        when(reportService.exportCsvForUser(1L)).thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/reports/my/export").param("userId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Export failed")));
    }
}

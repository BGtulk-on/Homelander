package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.exception.GlobalExceptionHandler;
import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.models.EquipmentCondition;
import com.uktc.schoolInventory.models.EquipmentStatus;
import com.uktc.schoolInventory.services.EquipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EquipmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EquipmentService service;

    @InjectMocks
    private EquipmentController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Equipment createEquipment(Long id, String name) {
        Equipment e = new Equipment();
        e.setId(id);
        e.setName(name);
        e.setSerialNumber("SN-" + id);
        e.setStatus(EquipmentStatus.Available);
        e.setCurrentCondition(EquipmentCondition.GOOD);
        return e;
    }

    @Test
    void getAll_returnsOk() throws Exception {
        when(service.getAllEquipment()).thenReturn(List.of(
                createEquipment(1L, "Laptop"),
                createEquipment(2L, "Projector")
        ));

        mockMvc.perform(get("/api/equipment/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void create_returnsEquipment() throws Exception {
        Equipment saved = createEquipment(1L, "Tablet");
        when(service.createEquipment(any(Equipment.class))).thenReturn(saved);

        mockMvc.perform(post("/api/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tablet\",\"serialNumber\":\"SN-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tablet"));
    }

    @Test
    void update_existingId_returnsUpdated() throws Exception {
        Equipment updated = createEquipment(1L, "Updated Laptop");
        when(service.updateEquipment(eq(1L), any(Equipment.class))).thenReturn(updated);

        mockMvc.perform(put("/api/equipment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Laptop\",\"serialNumber\":\"SN-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(service.updateEquipment(eq(999L), any(Equipment.class)))
                .thenThrow(new ResourceNotFoundException("Equipment not found with id: 999"));

        mockMvc.perform(put("/api/equipment/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"serialNumber\":\"SN-X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsOk() throws Exception {
        doNothing().when(service).deleteEquipment(1L);

        mockMvc.perform(delete("/api/equipment/1"))
                .andExpect(status().isOk());
    }

    @Test
    void assign_returnsUpdated() throws Exception {
        Equipment assigned = createEquipment(1L, "Laptop");
        assigned.setAssigned(true);
        assigned.setAssignedTo("Ivan");
        when(service.assignEquipment(1L, "Ivan")).thenReturn(assigned);

        mockMvc.perform(patch("/api/equipment/1/assign")
                        .param("personName", "Ivan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").value("Ivan"))
                .andExpect(jsonPath("$.assigned").value(true));
    }

    @Test
    void assign_notFound_returns404() throws Exception {
        when(service.assignEquipment(999L, "Ivan"))
                .thenThrow(new ResourceNotFoundException("Equipment not found with id: 999"));

        mockMvc.perform(patch("/api/equipment/999/assign")
                        .param("personName", "Ivan"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLowStock_returnsOk() throws Exception {
        when(service.getLowStockEquipment()).thenReturn(List.of(createEquipment(1L, "Scarce Item")));

        mockMvc.perform(get("/api/equipment/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_returnsResults() throws Exception {
        when(service.searchEquipment(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(createEquipment(1L, "Laptop")));

        mockMvc.perform(post("/api/equipment/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"Laptop\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}

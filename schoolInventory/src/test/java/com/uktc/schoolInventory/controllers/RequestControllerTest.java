package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.controllers.user.Role;
import com.uktc.schoolInventory.exception.GlobalExceptionHandler;
import com.uktc.schoolInventory.models.*;
import com.uktc.schoolInventory.repositories.RequestRepository;
import com.uktc.schoolInventory.services.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RequestService requestService;

    @Mock
    private RequestRepository repository;

    @InjectMocks
    private RequestController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Request createRequest(Long id) {
        Request r = new Request();
        r.setId(id);
        User u = new User();
        u.setId(1L);
        u.setEmail("test@example.com");
        u.setRole(Role.USER);
        r.setUser(u);
        Equipment e = new Equipment();
        e.setId(1L);
        e.setName("Laptop");
        r.setEquipment(e);
        r.setRequestStatus(RequestStatusType.PENDING);
        r.setRequestedStartDate(OffsetDateTime.now());
        r.setRequestedEndDate(OffsetDateTime.now().plusDays(7));
        return r;
    }

    @Test
    void getRequestsByUser_returnsOk() throws Exception {
        when(repository.findAllByUser_Id(1L)).thenReturn(List.of(createRequest(1L)));

        mockMvc.perform(get("/api/requests/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllRequests_returnsOk() throws Exception {
        when(repository.findAll()).thenReturn(List.of(createRequest(1L), createRequest(2L)));

        mockMvc.perform(get("/api/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createRequest_returnsCreated() throws Exception {
        Request saved = createRequest(1L);
        when(requestService.createRequest(any(Request.class))).thenReturn(saved);

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user\":{\"id\":1},\"equipment\":{\"id\":1},\"requestedStartDate\":\"2026-03-26T10:00:00+02:00\",\"requestedEndDate\":\"2026-04-02T10:00:00+02:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void approveRequest_returnsOk() throws Exception {
        Request request = createRequest(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.save(any(Request.class))).thenReturn(request);

        mockMvc.perform(put("/api/requests/1/approve")
                        .param("adminId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void approveRequest_notFound_returns404() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/requests/999/approve")
                        .param("adminId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectRequest_returnsOk() throws Exception {
        Request request = createRequest(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.save(any(Request.class))).thenReturn(request);

        mockMvc.perform(put("/api/requests/1/reject")
                        .param("adminId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void returnEquipment_returnsOk() throws Exception {
        Request request = createRequest(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.save(any(Request.class))).thenReturn(request);

        mockMvc.perform(put("/api/requests/1/return")
                        .param("condition", "GOOD"))
                .andExpect(status().isOk());
    }

    @Test
    void returnEquipment_notFound_returns404() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/requests/999/return")
                        .param("condition", "GOOD"))
                .andExpect(status().isNotFound());
    }
}

package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.*;
import com.uktc.schoolInventory.repositories.EquipmentRepository;
import com.uktc.schoolInventory.repositories.RequestRepository;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository repository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private RequestService requestService;

    private User createUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail("test@example.com");
        u.setPasswordHash("hashed");
        return u;
    }

    private Equipment createEquipment(Long id) {
        Equipment e = new Equipment();
        e.setId(id);
        e.setName("Laptop");
        e.setSerialNumber("SN-001");
        return e;
    }

    private Request createRequest() {
        Request r = new Request();
        User userRef = new User();
        userRef.setId(1L);
        Equipment equipRef = new Equipment();
        equipRef.setId(1L);
        r.setUser(userRef);
        r.setEquipment(equipRef);
        r.setRequestedStartDate(OffsetDateTime.now());
        r.setRequestedEndDate(OffsetDateTime.now().plusDays(7));
        return r;
    }

    @Test
    void createRequest_success_savesAndSendsEmail() {
        Request request = createRequest();
        User user = createUser(1L);
        Equipment equipment = createEquipment(1L);
        Request saved = createRequest();
        saved.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.save(any(Request.class))).thenReturn(saved);

        Request result = requestService.createRequest(request);

        assertNotNull(result);
        assertEquals(RequestStatusType.PENDING, request.getRequestStatus());
        verify(repository).save(request);
        verify(emailService).sendRentalConfirmation(user.getEmail(), equipment.getName());
    }

    @Test
    void createRequest_emailFails_stillSaves() {
        Request request = createRequest();
        User user = createUser(1L);
        Equipment equipment = createEquipment(1L);
        Request saved = createRequest();
        saved.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.save(any(Request.class))).thenReturn(saved);
        doThrow(new RuntimeException("SMTP error")).when(emailService)
                .sendRentalConfirmation(anyString(), anyString());

        Request result = requestService.createRequest(request);

        assertNotNull(result);
        verify(repository).save(request);
    }

    @Test
    void createRequest_userNotFound_throwsException() {
        Request request = createRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> requestService.createRequest(request));
    }

    @Test
    void createRequest_equipmentNotFound_throwsException() {
        Request request = createRequest();
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> requestService.createRequest(request));
    }
}

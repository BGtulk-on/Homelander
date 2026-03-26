package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.models.EquipmentCondition;
import com.uktc.schoolInventory.models.EquipmentStatus;
import com.uktc.schoolInventory.repositories.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository repository;

    @InjectMocks
    private EquipmentService service;

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
    void getAllEquipment_returnsList() {
        Equipment e1 = createEquipment(1L, "Laptop");
        Equipment e2 = createEquipment(2L, "Projector");
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        List<Equipment> result = service.getAllEquipment();

        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
    }

    @Test
    void createEquipment_savesAndReturns() {
        Equipment input = createEquipment(null, "Tablet");
        Equipment saved = createEquipment(1L, "Tablet");
        when(repository.save(input)).thenReturn(saved);

        Equipment result = service.createEquipment(input);

        assertEquals(1L, result.getId());
        verify(repository).save(input);
    }

    @Test
    void updateEquipment_existingId_updatesAllFields() {
        Equipment existing = createEquipment(1L, "Old Name");
        Equipment details = createEquipment(1L, "New Name");
        details.setAssignedTo("John");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Equipment.class))).thenReturn(existing);

        service.updateEquipment(1L, details);

        assertEquals("New Name", existing.getName());
        assertTrue(existing.isAssigned());
        verify(repository).save(existing);
    }

    @Test
    void updateEquipment_notFound_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateEquipment(999L, new Equipment()));
    }

    @Test
    void deleteEquipment_callsRepository() {
        doNothing().when(repository).deleteById(1L);

        service.deleteEquipment(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void assignEquipment_setsAssignedTrue() {
        Equipment equipment = createEquipment(1L, "Laptop");
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.save(any(Equipment.class))).thenReturn(equipment);

        Equipment result = service.assignEquipment(1L, "Ivan");

        assertTrue(result.isAssigned());
        assertEquals("Ivan", result.getAssignedTo());
    }

    @Test
    void assignEquipment_notFound_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.assignEquipment(999L, "Ivan"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchEquipment_withQuery_appliesSpecification() {
        Equipment e = createEquipment(1L, "Laptop");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(e));

        List<Equipment> result = service.searchEquipment("Laptop", null, null, null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchEquipment_allNull_returnsAll() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        List<Equipment> result = service.searchEquipment(null, null, null, null, null, null, null);

        assertTrue(result.isEmpty());
    }
}

package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.controllers.user.Role;
import com.uktc.schoolInventory.exception.GlobalExceptionHandler;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private User createUser(Long id, String firstName, Role role) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName("Test");
        u.setEmail(firstName.toLowerCase() + "@example.com");
        u.setPasswordHash("hashed");
        u.setRole(role);
        u.setApproved(false);
        return u;
    }

    @Test
    void getAllUsers_returnsOk() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(
                createUser(1L, "Ivan", Role.SUPERUSER),
                createUser(2L, "Maria", Role.USER)
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void approveUser_existingUser_returnsOk() throws Exception {
        User user = createUser(2L, "Maria", Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/2/approve"))
                .andExpect(status().isOk())
                .andExpect(content().string("User Maria approved successfully."));
    }

    @Test
    void approveUser_notFound_returns404() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/999/approve"))
                .andExpect(status().isNotFound());
    }

    @Test
    void makeAdmin_existingUser_returnsOk() throws Exception {
        User user = createUser(2L, "Maria", Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/2/make-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("User Maria is now an administrator."));
    }

    @Test
    void makeAdmin_notFound_returns404() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/999/make-admin"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_existingUser_returnsOk() throws Exception {
        when(userRepository.existsById(2L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(2L);

        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("User with ID 2 deleted successfully."));
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        when(userRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }
}

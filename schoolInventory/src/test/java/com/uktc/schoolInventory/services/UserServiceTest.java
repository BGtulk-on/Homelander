package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.controllers.user.Role;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User createUser(Long id, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Ivan");
        u.setLastName("Petrov");
        u.setEmail(email);
        u.setPasswordHash("encodedPassword");
        u.setRole(role);
        return u;
    }

    // ==================== saveUser ====================

    @Test
    void saveUser_encodesPasswordAndSaves() {
        User user = new User();
        user.setFirstName("Ivan");
        user.setEmail("ivan@example.com");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User saved = userService.saveUser(user, "rawPassword");

        assertEquals("encodedPassword", saved.getPasswordHash());
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(user);
    }

    // ==================== loadUserByUsername ====================

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        User user = createUser(1L, "ivan@example.com", Role.USER);

        when(userRepository.findByEmail("ivan@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("ivan@example.com");

        assertEquals("ivan@example.com", details.getUsername());
        assertEquals("encodedPassword", details.getPassword());
        assertFalse(details.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_adminUser_hasAdminAuthorities() {
        User user = createUser(2L, "admin@example.com", Role.ADMIN);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("admin@example.com");

        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_superUser_hasSuperuserAuthorities() {
        User user = createUser(3L, "super@example.com", Role.SUPERUSER);

        when(userRepository.findByEmail("super@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("super@example.com");

        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERUSER")));
    }

    @Test
    void loadUserByUsername_notFound_throwsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknown@example.com"));
    }
}

package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.controllers.user.Role;
import com.uktc.schoolInventory.dto.UserDto;
import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.UserRegisterDto;
import com.uktc.schoolInventory.exception.DuplicateResourceException;
import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success_returnsUserDto() {
        UserRegisterDto dto = new UserRegisterDto("Ivan", "Petrov", "ivan@example.com", "password");
        when(userRepository.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName("Ivan");
        savedUser.setLastName("Petrov");
        savedUser.setEmail("ivan@example.com");
        savedUser.setPasswordHash("encodedPassword");
        savedUser.setRole(Role.USER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = authService.register(dto);

        assertEquals("ivan@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        UserRegisterDto dto = new UserRegisterDto("Ivan", "Petrov", "ivan@example.com", "password");
        when(userRepository.findByEmail("ivan@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_success_returnsUserDto() {
        UserLoginDto dto = new UserLoginDto("ivan@example.com", "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // authenticate doesn't need to return anything on success

        User user = new User();
        user.setId(1L);
        user.setEmail("ivan@example.com");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("ivan@example.com")).thenReturn(Optional.of(user));

        UserDto result = authService.authenticate(dto);

        assertEquals("ivan@example.com", result.getEmail());
    }

    @Test
    void authenticate_badCredentials_throwsException() {
        UserLoginDto dto = new UserLoginDto("ivan@example.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(dto));
    }

    @Test
    void authenticate_userNotFound_throwsException() {
        UserLoginDto dto = new UserLoginDto("nobody@example.com", "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.authenticate(dto));
    }
}

package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.controllers.user.Role;
import com.uktc.schoolInventory.dto.UserDto;
import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.UserRegisterDto;
import com.uktc.schoolInventory.exception.DuplicateResourceException;
import com.uktc.schoolInventory.exception.GlobalExceptionHandler;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private User createUser() {
        User u = new User();
        u.setId(1L);
        u.setFirstName("Ivan");
        u.setLastName("Petrov");
        u.setEmail("ivan@example.com");
        u.setPasswordHash("hashed");
        u.setRole(Role.USER);
        return u;
    }

    @Test
    void register_success_returnsCreated() throws Exception {
        UserDto userDto = new UserDto(createUser());

        when(authService.register(any(UserRegisterDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"first_name\":\"Ivan\",\"last_name\":\"Petrov\",\"email\":\"ivan@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        when(authService.register(any(UserRegisterDto.class)))
                .thenThrow(new DuplicateResourceException("A user with this email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"first_name\":\"Ivan\",\"last_name\":\"Petrov\",\"email\":\"ivan@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void login_success_returnsOk() throws Exception {
        UserDto userDto = new UserDto(createUser());

        when(authService.authenticate(any(UserLoginDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ivan@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void login_badCredentials_returns500() throws Exception {
        when(authService.authenticate(any(UserLoginDto.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ivan@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void logout_returnsOk() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("logout successful"));
    }

    @Test
    void getCurrentUser_withSession_returnsOk() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userEmail", "ivan@example.com");
        session.setAttribute("userRole", Role.USER);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getCurrentUser_noSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}

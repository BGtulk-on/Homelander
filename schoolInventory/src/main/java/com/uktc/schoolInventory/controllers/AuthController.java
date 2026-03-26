package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.UserRegisterDto;
import com.uktc.schoolInventory.dto.response.LoginResponse;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginDto userLoginDto, HttpSession session, HttpServletRequest request){
        User authendicatedUser = authService.authenticate(userLoginDto);

        session.setAttribute("userId",authendicatedUser.getId());
        session.setAttribute("userEmail",authendicatedUser.getEmail());
        session.setAttribute("userIsAdmin",authendicatedUser.getIsAdmin());

        // Set Spring Security authentication in context and session
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                authendicatedUser.getEmail(),
                authendicatedUser.getPasswordHash(), // Use password hash for principal
                List.of(new SimpleGrantedAuthority(authendicatedUser.getIsAdmin() ? "ROLE_ADMIN" : "ROLE_USER"))
            );
        authToken.setDetails(authendicatedUser); // Attach the full user object as details
        SecurityContextHolder.getContext().setAuthentication(authToken);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        LoginResponse loginResponse = new LoginResponse(
                authendicatedUser.getId(),
                authendicatedUser.getEmail(),
                authendicatedUser.getIsAdmin()
        );
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        authService.register(userRegisterDto);
        return ResponseEntity.ok().build();
    }
}

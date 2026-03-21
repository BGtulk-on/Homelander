package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.response.LoginResponse;
import com.uktc.schoolInventory.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginDto loginDto) {
        var user = authService.authenticate(loginDto);
        var response = new LoginResponse(user.getId(), user.getEmail(), user.getIsAdmin());
        return ResponseEntity.ok(response);
    }
}

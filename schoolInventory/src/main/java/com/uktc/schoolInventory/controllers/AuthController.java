package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.dto.UserDto;
import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.UserRegisterDto;
import com.uktc.schoolInventory.dto.response.LoginResponse;
import com.uktc.schoolInventory.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserRegisterDto userRegisterDto){
        UserDto registeredUser = authService.register(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginDto userLoginDto, HttpSession session, HttpServletRequest request){

        UserDto authendicatedUser = authService.authenticate(userLoginDto);

        session.setAttribute("userEmail",authendicatedUser.getEmail());
        session.setAttribute("userRole",authendicatedUser.getRole());

        // Set Spring Security authentication in context and session
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                authendicatedUser.getEmail(),
                null, // No password needed here as it's already authenticated
                authendicatedUser.getRole().getAuthorities()
            );
        authToken.setDetails(authendicatedUser); // Attach the full user object as details
        SecurityContextHolder.getContext().setAuthentication(authToken);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        LoginResponse loginResponse = new LoginResponse(
                authendicatedUser.getId(),
                authendicatedUser.getEmail(),
                authendicatedUser.getRole()
        );
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request){
       HttpSession session = request.getSession(false);

        if(session != null){
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("logout successful");
    }

}

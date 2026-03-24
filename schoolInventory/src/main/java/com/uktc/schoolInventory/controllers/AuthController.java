package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.UserRegisterDto;
import com.uktc.schoolInventory.dto.response.LoginResponse;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.services.AuthService;
import com.uktc.schoolInventory.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public AuthController(UserService userService, AuthService authService){
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRegisterDto userRegisterDto){
        User registeredUser = authService.register(userRegisterDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginDto userLoginDto, HttpSession session, HttpServletRequest request){
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

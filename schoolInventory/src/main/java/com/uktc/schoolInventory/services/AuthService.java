package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.controllers.user.Role;
import com.uktc.schoolInventory.dto.UserDto;
import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.UserRegisterDto;
import com.uktc.schoolInventory.exception.DuplicateResourceException;
import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public UserDto register(UserRegisterDto input){
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        User user = new User();
        user.setFirstName(input.getFirst_name());
        user.setLastName(input.getLast_name());
        user.setEmail(input.getEmail());
        user.setPasswordHash(passwordEncoder.encode(input.getPassword()));
        user.setRole(Role.USER);

        User saveduser = userRepository.save(user);
        return new UserDto(saveduser);

    }

    public UserDto authenticate(UserLoginDto input){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );

        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email address"));

        return new UserDto(user);

        }

}



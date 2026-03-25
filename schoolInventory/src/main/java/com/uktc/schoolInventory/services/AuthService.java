package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.UserRegisterDto;
import com.uktc.schoolInventory.exception.BadRequestException;
import com.uktc.schoolInventory.exception.DuplicateResourceException;
import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.exception.UnauthorizedActionException;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(UserRegisterDto input){
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        User user = new User();
        user.setFirstName(input.getFirst_name());
        user.setLastName(input.getLast_name());
        user.setEmail(input.getEmail());
        user.setPasswordHash(passwordEncoder.encode(input.getPassword()));
        user.setIsAdmin(false);

        return userRepository.save(user);

    }

    public User authenticate(UserLoginDto input){
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email address"));

        if (!passwordEncoder.matches(input.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Incorrect email or password");
        }

        if (user.getApproved() == null || !user.getApproved()) {
            throw new UnauthorizedActionException("Your account is pending approval.");
        }

        return user;
    }
}



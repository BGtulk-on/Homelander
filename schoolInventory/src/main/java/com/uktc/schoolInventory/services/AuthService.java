package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.dto.UserLoginDto;
import com.uktc.schoolInventory.dto.UserRegisterDto;
import com.uktc.schoolInventory.exception.BadRequestException;
import com.uktc.schoolInventory.exception.DuplicateResourceException;
import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private UserRepository userRepository;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserService userService,
                       AuthenticationManager authenticationManager
    ){
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User register(UserRegisterDto input){
        if (userRepository.findUserByEmail(input.getEmail()).isPresent()) {
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
        User user = userRepository.findUserByEmail(input.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email address"));

        if (!passwordEncoder.matches(input.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Incorrect email or password");
        }

        return user;
    }
}



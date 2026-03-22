package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.controllers.user.Role;
import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.exception.UnauthorizedActionException;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('ADMIN')")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Approve registration
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPERUSER')")
    public String approveUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return "User " + user.getFirstName() + " approved successfully.";
    }

    // Grant admin rights
    @PutMapping("/{id}/make-admin")
    @PreAuthorize("hasRole('SUPERUSER')")
    public String makeAdmin(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        return "User " + user.getFirstName() + " is now an administrator.";
    }

    // Delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return "User with ID " + id + " deleted successfully.";
    }
}
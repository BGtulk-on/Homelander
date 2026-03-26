package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.exception.UnauthorizedActionException;
import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userRepository.save(user);
    }

    // Approve registration
    @PutMapping("/{id}/approve")
    public String approveUser(@PathVariable Long id, @RequestParam Long requesterId) {
        if (!requesterId.equals(1L)) {
            throw new UnauthorizedActionException("Only the Superuser can approve registrations");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return "User " + user.getFirstName() + " approved successfully.";
    }

    // Grant admin rights
    @PutMapping("/{id}/make-admin")
    public String makeAdmin(@PathVariable Long id, @RequestParam Long requesterId) {
        if (!requesterId.equals(1L)) {
            throw new UnauthorizedActionException("Only the Superuser can grant admin rights");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsAdmin(true);
        userRepository.save(user);
        return "User " + user.getFirstName() + " is now an administrator.";
    }

    // Delete user
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id, @RequestParam Long requesterId) {
        if (!requesterId.equals(1L)) {
            throw new UnauthorizedActionException("Only the Superuser can delete users");
        }
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return "User with ID " + id + " deleted successfully.";
    }
}
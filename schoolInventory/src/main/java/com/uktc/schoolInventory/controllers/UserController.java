package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.models.User;
import com.uktc.schoolInventory.repositories.UserRepository;
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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Одобряване на регистрация
    @PutMapping("/{id}/approve")
    public String approveUser(@PathVariable Long id, @RequestParam Long requesterId) {
        if (!requesterId.equals(1L)) {
            return "Error: Unauthorized. Only Superuser can approve registrations.";
        }
        return userRepository.findById(id)
                .map(user -> "User " + user.getFirstName() + " approved successfully.")
                .orElse("Error: User not found.");
    }

    // Повишаване в администратор
    @PutMapping("/{id}/make-admin")
    public String makeAdmin(@PathVariable Long id, @RequestParam Long requesterId) {
        if (!requesterId.equals(1L)) {
            return "Error: Unauthorized. Only Superuser can grant admin rights.";
        }
        return userRepository.findById(id)
                .map(user -> {
                    user.setIsAdmin(true);
                    userRepository.save(user);
                    return "User " + user.getFirstName() + " is now an administrator.";
                })
                .orElse("Error: User not found.");
    }

    // Изтриване на потребител
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id, @RequestParam Long requesterId) {
        if (!requesterId.equals(1L)) {
            return "Error: Unauthorized. Only Superuser can delete users.";
        }
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return "User with ID " + id + " deleted successfully.";
        }
        return "Error: User not found.";
    }
}
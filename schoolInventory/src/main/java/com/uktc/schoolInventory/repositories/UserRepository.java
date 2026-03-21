package com.uktc.schoolInventory.repositories;

import com.uktc.schoolInventory.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findUserByEmail(String email);
    boolean existsByEmail(String email);
}
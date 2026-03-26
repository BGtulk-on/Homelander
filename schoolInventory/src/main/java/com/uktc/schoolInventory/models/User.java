package com.uktc.schoolInventory.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users") // Свързва се с таблицата users в SQL [cite: 58]
@Data // Lombok автоматично прави getter/setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "isadmin")
    private Boolean isAdmin = false;

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
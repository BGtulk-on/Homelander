package com.uktc.schoolInventory.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class  UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isAdmin;
    private Boolean approved;
    private OffsetDateTime createdAt;

    public UserResponse(Long id, String firstName, String lastName, String email, Boolean isAdmin, Boolean approved, OffsetDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isAdmin = isAdmin;
        this.approved = approved;
        this.createdAt = createdAt;
    }
}

package com.uktc.schoolInventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String email;
    private Boolean isAdmin;
    private String firstName;
}

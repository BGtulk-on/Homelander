package com.uktc.schoolInventory.dto.response;

import com.uktc.schoolInventory.controllers.user.Role;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class LoginResponse {
    private Long id;
    private String email;
    private Role role;
}

package com.uktc.schoolInventory.dto;

import com.uktc.schoolInventory.controllers.user.Role;
import com.uktc.schoolInventory.models.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UserDto {
    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private Role role;

    public  UserDto(User user) {
        this.id = user.getId();
        this.first_name = user.getFirstName();
        this.last_name = user.getLastName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}

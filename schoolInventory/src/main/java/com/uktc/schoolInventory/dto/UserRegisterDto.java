package com.uktc.schoolInventory.dto;

import lombok.Data;

@Data
public class UserRegisterDto {
    private String first_name;
    private String last_name;
    private String email;
    private String password;
}

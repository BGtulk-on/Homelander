package com.uktc.schoolInventory.dto;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class UserRegisterDto {
    private String first_name;
    private String last_name;
    private String email;
    private String password;
}

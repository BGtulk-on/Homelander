package com.uktc.schoolInventory.dto;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserRegisterDto {
    private String first_name;
    private String last_name;
    private String email;
    private String password;
}

package com.uktc.schoolInventory.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class UserLoginDto {
    private String email;
    private String password;
}

package com.uktc.schoolInventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginDto {
//    @NotBlank(message = "Email is required")
//    @Email(message = "Invalid email format")
//    @Pattern(regexp = "^[^<>&\"']*$", message = "Email contains invalid characters")
    private String email;

//    @NotBlank(message = "Password is required")
//    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
//    @Pattern(regexp = "^[^<>&\"']*$", message = "Password contains invalid characters")
    private String password;
}

package com.jewelvaulterp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String username,
        @NotBlank @Email String email
) {
}

package com.jewelvaulterp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserRequest(
        @NotNull UUID companyId,
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}

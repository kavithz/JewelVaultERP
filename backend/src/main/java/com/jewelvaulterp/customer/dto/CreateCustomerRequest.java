package com.jewelvaulterp.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCustomerRequest(

        @NotNull
        UUID companyId,

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 50)
        String code,

        @Size(max = 50)
        String phone,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 255)
        String address,

        @Size(max = 100)
        String taxNumber
) {
}
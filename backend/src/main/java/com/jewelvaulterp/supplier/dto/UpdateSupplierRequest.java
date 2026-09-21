package com.jewelvaulterp.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSupplierRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 50)
        String code,

        @Size(max = 150)
        String contactPerson,

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
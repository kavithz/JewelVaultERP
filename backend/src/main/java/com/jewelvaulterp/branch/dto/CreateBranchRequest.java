package com.jewelvaulterp.branch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBranchRequest(

        @NotNull
        UUID companyId,

        @NotBlank
        String name,

        @NotBlank
        String code,

        String address,

        String city,

        String countryCode,

        String phone,

        @Email
        String email
) {
}
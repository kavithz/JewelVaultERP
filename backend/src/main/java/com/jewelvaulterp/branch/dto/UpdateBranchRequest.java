package com.jewelvaulterp.branch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateBranchRequest(

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
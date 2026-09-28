package com.jewelvaulterp.auth.config;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ProtectedProbeController {

    @GetMapping("/api/security-probe")
    String probe() {
        return "authenticated";
    }

    @GetMapping("/api/permission-probe")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    String permissionProbe() {
        return "authorized";
    }

    @GetMapping("/api/user-scope-probe/{userId}")
    String userProbe(@PathVariable java.util.UUID userId) {
        return userId.toString();
    }

    @GetMapping("/api/admin-probe")
    @PreAuthorize("hasRole('ADMIN')")
    String adminProbe() {
        return "admin";
    }

    @GetMapping("/api/company-scope-probe/{companyId}")
    String companyProbe(@PathVariable java.util.UUID companyId) {
        return companyId.toString();
    }

    @PostMapping("/api/company-body-probe")
    String companyBodyProbe(@RequestBody AuthSecurityIntegrationTest.CompanyProbeRequest request) {
        return request.companyId().toString();
    }
}
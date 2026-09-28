package com.jewelvaulterp.company.controller;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.service.CompanyService;
import com.jewelvaulterp.auth.service.CurrentUserAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final CurrentUserAccess currentUserAccess;

    public CompanyController(CompanyService companyService, CurrentUserAccess currentUserAccess) {
        this.companyService = companyService;
        this.currentUserAccess = currentUserAccess;
    }

    @GetMapping
    public List<Company> getCompanies() {
        return companyService.getCompany(currentUserAccess.companyId())
            .map(List::of)
            .orElseGet(List::of);
    }
}

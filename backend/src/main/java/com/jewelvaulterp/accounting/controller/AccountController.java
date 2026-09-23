package com.jewelvaulterp.accounting.controller;

import com.jewelvaulterp.accounting.dto.AccountResponse;
import com.jewelvaulterp.accounting.dto.CreateAccountRequest;
import com.jewelvaulterp.accounting.dto.UpdateAccountRequest;
import com.jewelvaulterp.accounting.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountResponse> getAll() {
        return accountService.getAll();
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable UUID id) {
        return accountService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return accountService.create(request);
    }

    @PutMapping("/{id}")
    public AccountResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        return accountService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public AccountResponse setActive(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return accountService.setActive(id, active);
    }
}
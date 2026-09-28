package com.jewelvaulterp.user.controller;

import com.jewelvaulterp.user.dto.CreateUserRequest;
import com.jewelvaulterp.user.dto.UpdateUserRequest;
import com.jewelvaulterp.user.dto.UserResponse;
import com.jewelvaulterp.auth.service.CurrentUserAccess;
import com.jewelvaulterp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserAccess currentUserAccess;

    public UserController(UserService userService, CurrentUserAccess currentUserAccess) {
        this.userService = userService;
        this.currentUserAccess = currentUserAccess;
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getUsersByCompany(currentUserAccess.companyId());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        currentUserAccess.requireCompany(request.companyId());
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(
            currentUserAccess.requireUserAccess(id),
            currentUserAccess.companyId(),
            request
        );
    }
}

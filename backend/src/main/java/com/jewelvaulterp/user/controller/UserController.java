package com.jewelvaulterp.user.controller;

import com.jewelvaulterp.user.dto.CreateUserRequest;
import com.jewelvaulterp.user.dto.UpdateUserRequest;
import com.jewelvaulterp.user.dto.UserResponse;
import com.jewelvaulterp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }
}

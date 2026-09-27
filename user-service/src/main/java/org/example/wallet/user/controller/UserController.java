package org.example.wallet.user.controller;

import jakarta.validation.Valid;
import org.example.wallet.user.dto.CreateUserRequest;
import org.example.wallet.user.dto.UpdateUserRequest;
import org.example.wallet.user.dto.UpdateUserStatusRequest;
import org.example.wallet.user.dto.UserResponse;
import org.example.wallet.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return userService.createUser(request);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable UUID id) {

        return userService.getUserById(id);
    }

    // GET ALL
    @GetMapping
    public Page<UserResponse> getUsers(
            Pageable pageable) {

        return userService.getUsers(pageable);
    }

    // UPDATE
    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {

        return userService.updateUser(id, request);
    }

    // UPDATE STATUS
    @PatchMapping("/{id}/status")
    public UserResponse updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        return userService.updateUserStatus(
                id,
                request.getStatus()
        );
    }

    // SOFT DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(
            @PathVariable UUID id) {

        userService.deactivateUser(id);
    }
}
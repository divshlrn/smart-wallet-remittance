package org.example.wallet.user.controller;

import jakarta.validation.Valid;
import org.example.wallet.user.dto.CreateUserRequest;
import org.example.wallet.user.dto.UserResponse;
import org.example.wallet.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
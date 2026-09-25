package org.example.wallet.user.dto;

import org.example.wallet.user.entity.User;
import org.example.wallet.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private Instant createdAt;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();

        response.id = user.getId();
        response.firstName = user.getFirstName();
        response.lastName = user.getLastName();
        response.email = user.getEmail();
        response.phoneNumber = user.getPhoneNumber();
        response.status = user.getStatus();
        response.createdAt = user.getCreatedAt();

        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
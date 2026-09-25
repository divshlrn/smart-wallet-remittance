package org.example.wallet.user.service;

import org.example.wallet.user.dto.CreateUserRequest;
import org.example.wallet.user.dto.UserResponse;
import org.example.wallet.user.entity.User;
import org.example.wallet.user.entity.UserStatus;
import org.example.wallet.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}

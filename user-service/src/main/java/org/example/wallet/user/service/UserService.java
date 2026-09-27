package org.example.wallet.user.service;

import org.example.wallet.user.dto.CreateUserRequest;
import org.example.wallet.user.dto.UpdateUserRequest;
import org.example.wallet.user.dto.UserResponse;
import org.example.wallet.user.entity.User;
import org.example.wallet.user.entity.UserStatus;
import org.example.wallet.user.exception.UserAlreadyExistsException;
import org.example.wallet.user.exception.UserNotFoundException;
import org.example.wallet.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already registered: " + request.getEmail()
            );
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException(
                    "Phone number already registered: " + request.getPhoneNumber()
            );
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

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {

        return userRepository.findAll(pageable)
                .map(UserResponse::from);
    }

    // UPDATE
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException(
                        "Email already registered: " + request.getEmail()
                );
            }

            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(user.getPhoneNumber())) {

            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new UserAlreadyExistsException(
                        "Phone number already registered: "
                                + request.getPhoneNumber()
                );
            }

            user.setPhoneNumber(request.getPhoneNumber());
        }

        return UserResponse.from(user);
    }

    // UPDATE STATUS
    @Transactional
    public UserResponse updateUserStatus(
            UUID id,
            UserStatus status) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        user.setStatus(status);

        return UserResponse.from(user);
    }

    // SOFT DELETE
    @Transactional
    public void deactivateUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        user.setStatus(UserStatus.INACTIVE);
    }
}

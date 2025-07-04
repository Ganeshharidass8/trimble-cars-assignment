package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.exception.BusinessRuleViolationException;
import com.trimblecars.lease_service.exception.ResourceAlreadyExistsException;
import com.trimblecars.lease_service.exception.ResourceNotFoundException;
import com.trimblecars.lease_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        log.info("Attempting to register user: {}", user.getEmail());

        userRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
            throw new ResourceAlreadyExistsException("User with this email already exists: " + user.getEmail());
        });

        if (user.getRole() == null) {
            log.info("No role provided. Defaulting to CUSTOMER");
            user.setRole(UserRole.CUSTOMER);
        }

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());
        return savedUser;
    }


    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public void registerUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("User list cannot be null or empty");
        }

        users.forEach(this::registerUser); // ✅ Reuse your existing validated method
    }





}


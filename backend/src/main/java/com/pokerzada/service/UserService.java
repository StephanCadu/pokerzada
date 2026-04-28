package com.pokerzada.service;

import com.pokerzada.entity.User;
import com.pokerzada.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user
     * Validations:
     * - Name cannot be blank
     * - Email cannot be blank and must be valid
     * - Email must be unique
     * - Password cannot be blank
     */
    @Transactional
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }

        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email format is invalid");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }

        user.setActive(true);
        user.setDeleted(false);

        return userRepository.save(user);
    }

    /**
     * Updates an existing user
     * Validations:
     * - User must exist and not be deleted
     * - Name cannot be blank
     * - Email cannot be blank and must be valid
     * - Email must be unique (excluding the user being updated)
     * - Password cannot be blank
     */
    @Transactional
    public User update(Long id, User userUpdate) {
        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found or has been deleted"));

        if (userUpdate.getName() != null && !userUpdate.getName().isBlank()) {
            user.setName(userUpdate.getName());
        }

        if (userUpdate.getEmail() != null && !userUpdate.getEmail().isBlank()) {
            if (!userUpdate.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Email format is invalid");
            }

            if (!user.getEmail().equals(userUpdate.getEmail())) {
                if (userRepository.findByEmail(userUpdate.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("Email already exists");
                }
                user.setEmail(userUpdate.getEmail());
            }
        }

        if (userUpdate.getPassword() != null && !userUpdate.getPassword().isBlank()) {
            user.setPassword(userUpdate.getPassword());
        }

        return userRepository.save(user);
    }

    /**
     * Gets a user by ID
     * Validations:
     * - User must exist and not be deleted
     */
    public Optional<User> get(Long id) {
        return userRepository.findByIdAndNotDeleted(id);
    }

    /**
     * Lists all non-deleted users
     */
    public List<User> list() {
        return userRepository.findAllActive();
    }

    /**
     * Toggles the active status of a user
     * Validations:
     * - User must exist and not be deleted
     */
    @Transactional
    public User toggleActive(Long id) {
        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found or has been deleted"));

        user.setActive(!user.getActive());
        return userRepository.save(user);
    }

    /**
     * Soft deletes a user (marks as deleted)
     * Validations:
     * - User must exist
     */
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setDeleted(true);
        userRepository.save(user);
    }

}

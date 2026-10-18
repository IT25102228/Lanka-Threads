package com.lankathreads.service;

import com.lankathreads.model.User;
import com.lankathreads.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service handling Factory Staff User accounts and authentication credentials.
 */
@SuppressWarnings("null")
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Saves a new user, encoding the raw password with BCrypt if needed.
     */
    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * Saves changes to an existing user without modifying the encoded password.
     */
    public User saveExistingUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves all system users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by username.
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by email.
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds a user by either username or email.
     */
    public Optional<User> getUserByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier, identifier);
    }

    /**
     * Finds a user by primary key ID.
     */
    @Nullable
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Deletes a user by ID.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}

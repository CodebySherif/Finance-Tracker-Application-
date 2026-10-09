package com.financetracker.service;

import com.financetracker.model.AuthProvider;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("That username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("That email is already registered");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setProvider(AuthProvider.LOCAL);
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    /**
     * Called after a successful Google sign-in. The user's Google email
     * doubles as their username here - it's unique by construction, so there's
     * no collision logic to worry about. Returns the existing account if this
     * email has signed in before, or creates a fresh (passwordless) one.
     */
    public User findOrCreateGoogleUser(String email) {
        return userRepository.findByUsername(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(email);
                    user.setEmail(email);
                    user.setPassword(null);
                    user.setProvider(AuthProvider.GOOGLE);
                    return userRepository.save(user);
                });
    }
}

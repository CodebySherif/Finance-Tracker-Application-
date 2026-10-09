package com.financetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Validation (NotBlank/Email/Size) lives on the request DTOs (RegisterRequest,
 * LoginRequest), not here. Hibernate auto-validates entities on persist when
 * Bean Validation annotations are present on the entity itself, which would
 * break Google-authenticated users (who have no local password) if those
 * constraints were declared here instead.
 */
@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    // Null for accounts created via Google Sign-In - they have no local password.
    @JsonIgnore
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}

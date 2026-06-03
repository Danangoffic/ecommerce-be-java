package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPasswordResetTokenAndTokenExpiryAfter(String token, Instant now);

    Optional<User> findByVerificationToken(String token);

    long countByRole(com.ecommerce.entity.enums.Role role);
}

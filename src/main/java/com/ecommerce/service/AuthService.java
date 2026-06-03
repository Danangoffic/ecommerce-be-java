package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.dto.response.UserProfileResponse;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(saved);
        return new AuthResponse(jwtService.generateToken(authenticatedUser), toProfile(saved));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId()).orElseThrow();
        return new AuthResponse(jwtService.generateToken(principal), toProfile(user));
    }

    @Transactional
    public AuthResponse oauth2Login(String name, String email, String provider, String providerId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(name);
                    newUser.setEmail(email.toLowerCase());
                    newUser.setRole(Role.CUSTOMER);
                    newUser.setStatus(UserStatus.ACTIVE);
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    return userRepository.save(newUser);
                });

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user);
        return new AuthResponse(jwtService.generateToken(authenticatedUser), toProfile(user));
    }

    public UserProfileResponse currentUser(Long userId) {
        return userRepository.findById(userId)
                .map(this::toProfile)
                .orElseThrow();
    }

    UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}

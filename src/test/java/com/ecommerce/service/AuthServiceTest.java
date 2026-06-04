package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.dto.response.UserProfileResponse;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("alice@example.com");
        testUser.setName("Alice");
        testUser.setPassword("encoded_password");
        testUser.setPhoneNumber("08123456789");
        testUser.setRole(Role.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(Instant.now());
    }

    @Test
    void registerSuccessfully() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123", "08123456789");
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(AuthenticatedUser.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("alice@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerThrowsExceptionWhenEmailExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123", "08123456789");
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void loginSuccessfully() {
        // Arrange
        LoginRequest request = new LoginRequest("alice@example.com", "password123");
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(testUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                authenticatedUser, null, authenticatedUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(authenticatedUser)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("alice@example.com");
    }

    @Test
    void oauth2LoginSuccessfully() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("oauthuser@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(AuthenticatedUser.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.oauth2Login("OAuth User", "oauthuser@example.com", "google", "google-id-123");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt-token");
    }

    @Test
    void oauth2LoginCreatesNewUserIfNotExists() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(AuthenticatedUser.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.oauth2Login("New User", "newuser@example.com", "google", "google-id-456");

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getCurrentUserReturnsUserProfile() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserProfileResponse response = authService.currentUser(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.role()).isEqualTo("CUSTOMER");
    }

    @Test
    void getCurrentUserThrowsExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.currentUser(999L))
                .isInstanceOf(Exception.class);
    }
}

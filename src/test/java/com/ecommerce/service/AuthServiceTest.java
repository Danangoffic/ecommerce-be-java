package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.dto.response.UserProfileResponse;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.entity.enums.UserStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private final Instant futureExpiry = Instant.now().plusSeconds(3600);

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

    private void stubIssueTokens() {
        when(jwtService.generateRefreshToken()).thenReturn("refresh-uuid");
        when(jwtService.refreshTokenExpiresAt()).thenReturn(futureExpiry);
        when(jwtService.generateToken(any(AuthenticatedUser.class))).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
    }

    @Test
    void registerSuccessfully() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123", "08123456789");
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        stubIssueTokens();

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-uuid");
        assertThat(response.user().email()).isEqualTo("alice@example.com");
        // save dipanggil 2x: sekali untuk create user, sekali di issueTokens untuk refresh token
        verify(userRepository, org.mockito.Mockito.times(2)).save(any(User.class));
    }

    @Test
    void registerThrowsExceptionWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123", "08123456789");
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void loginSuccessfully() {
        LoginRequest request = new LoginRequest("alice@example.com", "password123");
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(testUser);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        stubIssueTokens();

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-uuid");
    }

    @Test
    void oauth2LoginSuccessfully() {
        when(userRepository.findByEmailIgnoreCase("oauthuser@example.com")).thenReturn(Optional.of(testUser));
        stubIssueTokens();

        AuthResponse response = authService.oauth2Login("OAuth User", "oauthuser@example.com", "google", "google-id-123");

        assertThat(response.accessToken()).isEqualTo("jwt-token");
    }

    @Test
    void oauth2LoginCreatesNewUserIfNotExists() {
        when(userRepository.findByEmailIgnoreCase("newuser@example.com")).thenReturn(Optional.empty());
        stubIssueTokens();

        AuthResponse response = authService.oauth2Login("New User", "newuser@example.com", "google", "google-id-456");

        assertThat(response).isNotNull();
        // save dipanggil 2x: sekali untuk create user baru, sekali di issueTokens untuk refresh token
        verify(userRepository, org.mockito.Mockito.times(2)).save(any(User.class));
    }

    @Test
    void refreshSuccessfully() {
        testUser.setRefreshToken("valid-refresh-token");
        testUser.setRefreshTokenExpiry(futureExpiry);
        when(userRepository.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(testUser));
        when(jwtService.isRefreshTokenExpired(futureExpiry)).thenReturn(false);
        stubIssueTokens();

        AuthResponse response = authService.refresh("valid-refresh-token");

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-uuid");
    }

    @Test
    void refreshThrowsWhenTokenNotFound() {
        when(userRepository.findByRefreshToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("bad-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void refreshThrowsWhenTokenExpired() {
        Instant pastExpiry = Instant.now().minusSeconds(1);
        testUser.setRefreshToken("expired-token");
        testUser.setRefreshTokenExpiry(pastExpiry);
        when(userRepository.findByRefreshToken("expired-token")).thenReturn(Optional.of(testUser));
        when(jwtService.isRefreshTokenExpired(pastExpiry)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Refresh token expired");

        // token harus dihapus dari user saat expired
        assertThat(testUser.getRefreshToken()).isNull();
        assertThat(testUser.getRefreshTokenExpiry()).isNull();
    }

    @Test
    void getCurrentUserReturnsUserProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserProfileResponse response = authService.currentUser(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("alice@example.com");
    }

    @Test
    void getCurrentUserThrowsExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.currentUser(999L))
                .isInstanceOf(Exception.class);
    }
}

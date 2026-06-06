package com.ecommerce.dto.response;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UserProfileResponse user
) {
}

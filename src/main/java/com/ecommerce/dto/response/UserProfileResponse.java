package com.ecommerce.dto.response;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        String role,
        Instant createdAt
) {
}

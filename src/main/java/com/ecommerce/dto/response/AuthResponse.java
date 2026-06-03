package com.ecommerce.dto.response;

public record AuthResponse(String accessToken, UserProfileResponse user) {
}

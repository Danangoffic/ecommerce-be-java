package com.ecommerce.dto.request;

public record ResetPasswordRequest(String token, String newPassword) {}

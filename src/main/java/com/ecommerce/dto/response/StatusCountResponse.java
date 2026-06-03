package com.ecommerce.dto.response;

public record StatusCountResponse(
        String status,
        long total
) {
}

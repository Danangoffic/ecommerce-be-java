package com.ecommerce.dto.response;

public record ProductImageResponse(
        Long id,
        String imageUrl,
        Integer sortOrder,
        boolean primary
) {
}

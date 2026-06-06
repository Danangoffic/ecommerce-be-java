package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Integer minimumStockLevel,
        boolean purchasable,
        boolean lowStock,
        String imageUrl,
        String status,
        CategoryResponse category,
        WarehouseResponse warehouse,
        Double averageRating,
        Long reviewCount,
        boolean isInWishlist,
        boolean hasVariants,
        List<ProductImageResponse> images,
        List<ProductVariantResponse> variants,
        Instant createdAt
) {
}

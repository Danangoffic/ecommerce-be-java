package com.ecommerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpsertRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 150) String name,
        String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        @NotNull @Min(0) Integer stock,
        @Min(0) Integer minimumStockLevel,
        Long warehouseId,
        @Size(max = 500) String imageUrl,
        @NotBlank String status
) {
}

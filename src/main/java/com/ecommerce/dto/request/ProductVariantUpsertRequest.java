package com.ecommerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductVariantUpsertRequest(
        @NotBlank @Size(max = 80) String sku,
        @Size(max = 50) String size,
        @Size(max = 50) String color,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        @NotNull @Min(0) Integer stock,
        String status
) {
}

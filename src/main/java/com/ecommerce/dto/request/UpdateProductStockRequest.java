package com.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStockRequest(@NotNull @Min(0) Integer stock) {
}

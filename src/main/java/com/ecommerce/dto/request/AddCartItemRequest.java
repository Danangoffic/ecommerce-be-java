package com.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(@NotNull Long productId, Long variantId, @NotNull @Min(1) Integer quantity) {
}

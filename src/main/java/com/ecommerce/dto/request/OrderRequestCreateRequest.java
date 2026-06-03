package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrderRequestCreateRequest(
        @NotNull Long orderId,
        @NotBlank String type,
        @NotBlank @Size(max = 500) String reason,
        @Size(max = 1000) String notes,
        BigDecimal requestedAmount
) {
}

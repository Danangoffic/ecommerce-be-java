package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank @Size(max = 500) String shippingAddress,
        @NotBlank @Size(max = 100) String recipientName,
        @NotBlank @Size(max = 30) String recipientPhone,
        @Size(max = 500) String notes
) {
}

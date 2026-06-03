package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveOrderRequestRequest(
        @NotBlank String status,
        @Size(max = 1000) String adminNotes
) {
}

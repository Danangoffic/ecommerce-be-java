package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryUpsertRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @NotNull String status
) {
}

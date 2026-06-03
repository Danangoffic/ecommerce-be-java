package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProductStatusRequest(@NotBlank String status) {
}

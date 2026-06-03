package com.ecommerce.dto.response;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(boolean success, String message, List<String> errors, Instant timestamp) {
}

package com.ecommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailSendRequest(
        @NotBlank @Email String from,
        @NotBlank @Email String to,
        @NotBlank String subject,
        @NotBlank String html
) {
}

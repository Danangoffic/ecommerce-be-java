package com.ecommerce.controller;

import com.ecommerce.dto.request.EmailSendRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.service.ResendEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/emails")
@RequiredArgsConstructor
public class EmailController {

    private final ResendEmailService resendEmailService;

    @PostMapping("/send")
    public ApiResponse<Map<String, Object>> send(@Valid @RequestBody EmailSendRequest request) {
        Map<String, Object> resp = resendEmailService.sendEmail(request);
        return ApiResponse.success("Email sent", resp);
    }
}

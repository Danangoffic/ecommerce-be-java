package com.ecommerce.service;

import com.ecommerce.dto.request.EmailSendRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResendEmailService {

    private final ObjectMapper objectMapper;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> sendEmail(EmailSendRequest request) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException("Resend API key is not configured (resend.api.key)");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", request.from());
        payload.put("to", List.of(request.to()));
        payload.put("subject", request.subject());
        payload.put("html", request.html());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity("https://api.resend.com/emails", entity, String.class);

        Map<String, Object> result = new HashMap<>();
        result.put("statusCode", resp.getStatusCodeValue());
        result.put("body", resp.getBody());
        return result;
    }
}

package com.example.paymentauthorization.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.commonmodel.dto.AuthorizationRequest;
import com.example.commonmodel.dto.AuthorizationResponse;

@Service
public class AuthorizationService {

    private final RestTemplate restTemplate;

    @Value("${token-issuance.service.url}")
    private String tokenissuanceServiceUrl;

    public AuthorizationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AuthorizationResponse verify(AuthorizationRequest authorizationRequest) {
        // 외부 결제 승인사와 통신하여 결제 승인 요청
        String url = tokenissuanceServiceUrl + "/api/token/verify";

        try {
            ResponseEntity<AuthorizationResponse> response = restTemplate.postForEntity(url, authorizationRequest, AuthorizationResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Payment authorization succeeded for refId: " + authorizationRequest.getTokenId());
                return response.getBody();
            } else {
                System.err.println("Payment authorization failed for refId: " + authorizationRequest.getTokenId());
                throw new IllegalStateException("Failed to authorize payment");
            }
        } catch (Exception e) {
            System.err.println("Error during payment authorization: " + e.getMessage());
            throw new RuntimeException("Payment authorization error", e);
        }
    }
}

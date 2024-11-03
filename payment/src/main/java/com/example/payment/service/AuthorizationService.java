package com.example.payment.service;

import com.example.commonmodel.dto.AuthorizationRequest;
import com.example.commonmodel.dto.AuthorizationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthorizationService {

    private final RestTemplate restTemplate;

    @Value("${authorization.service.url}")
    private String authorizationServiceUrl;

    public AuthorizationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AuthorizationResponse requestPaymentAuthorization(String refId, String tokenId) {
        // 외부 결제 승인사와 통신하여 결제 승인 요청
        String url = authorizationServiceUrl + "/api/authorization/approve"; // 실제 결제 게이트웨이 API URL로 변경 필요

        // 결제 승인 요청에 필요한 데이터 구성
        AuthorizationRequest request = new AuthorizationRequest(refId, tokenId);

        try {
            // 결제 승인 요청 전송
            ResponseEntity<AuthorizationResponse> response = restTemplate.postForEntity(url, request, AuthorizationResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Payment authorization succeeded for refId: " + refId);
                return response.getBody();
            } else {
                System.err.println("Payment authorization failed for refId: " + refId);
                throw new IllegalStateException("Failed to authorize payment");
            }
        } catch (Exception e) {
            // 승인 요청 중 오류 발생 시 처리
            System.err.println("Error during payment authorization: " + e.getMessage());
            throw new RuntimeException("Payment authorization error", e);
        }

    }
}
package com.example.enduser.service;

import com.example.commonmodel.dto.AuthorizationResponse;
import com.example.commonmodel.dto.CardRegisterRequest;
import com.example.commonmodel.dto.TokenRequest;
import com.example.commonmodel.exception.AlreadyProgressForRefIdException;
import com.example.commonmodel.exception.CardAlreadyRegisteredException;
import com.example.commonmodel.exception.CardInfoNotFoundException;
import com.example.commonmodel.util.EncryptionUtil;
import com.example.commonmodel.util.HashUtil;
import com.example.enduser.dto.PaymentRequest;
import com.example.enduser.dto.RegisterRequest;
import com.example.enduser.dto.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    private final RestTemplate restTemplate;

    public UserService(RestTemplate restTemplate) {

        this.restTemplate = restTemplate;

    }

    public Response<?> registerCard(RegisterRequest registerRequest) {
        String url = paymentServiceUrl + "/api/payment/register";

        Response<Map<String, String>> response = new Response<>();

        try {
            byte[] iv = EncryptionUtil.generateIV();
            String encryptedCardNumber = EncryptionUtil.encrypt(registerRequest.getCardNumber(), iv);
            String encryptedCi = EncryptionUtil.encrypt(registerRequest.getCi(), iv);
            String cardHash = HashUtil.hash(registerRequest.getCardNumber());

            CardRegisterRequest cardRegisterRequest = new CardRegisterRequest(encryptedCi, encryptedCardNumber, cardHash);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, cardRegisterRequest, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {

                Map<String, String> data = new HashMap<>();

                data.put("refId", responseEntity.getBody());

                response.setCode(HttpStatus.OK);
                response.setData(data);
            } else {
                throw new IllegalStateException("Failed to register card: " + responseEntity.getBody());
            }

            return response;

        } catch (HttpClientErrorException e) {
            return handleClientError(e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register card due to an unexpected error: " + e.getMessage(), e);
        }
    }

    public Response<AuthorizationResponse> processPayment(PaymentRequest request) {
        String url = paymentServiceUrl + "/api/payment/process";
        Response<AuthorizationResponse> response = new Response<>();

        TokenRequest tokenRequest = new TokenRequest(request.getUserId(), request.getRefId(), request.getAmount());

        try {
            ResponseEntity<AuthorizationResponse> responseEntity = restTemplate.postForEntity(url, tokenRequest, AuthorizationResponse.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                response.setCode(HttpStatus.OK);
                response.setData(responseEntity.getBody());
            } else {
                throw new IllegalStateException("Failed to process payment: " + responseEntity.getBody());
            }

            return response;

        } catch (HttpClientErrorException e) {
            return handleClientError(e);

        } catch (Exception e) {
            // 다른 예외에 대한 처리
            throw new RuntimeException("Failed to register card due to an unexpected error: " + e.getMessage(), e);
        }
    }

    public Response<List<Map<String, Object>>> getTransaction(String refId) {
        Response<List<Map<String, Object>>> response = new Response<>();
        ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱을 위한 ObjectMapper

        try {
            // refId를 URL 변수로 전달
            String url = paymentServiceUrl + "/api/payment/transactions/{refId}";
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, refId);

            System.out.println("Response Body: " + responseEntity.getBody());

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                // JSON 응답 본문을 List<Map> 형식으로 변환
                List<Map<String, Object>> transactionData = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Map<String, Object>>>() {});
                response.setData(transactionData);
                response.setCode(HttpStatus.OK);
                return response;

            } else {
                throw new IllegalStateException("No Transaction found: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            // 예외 메시지를 포함한 에러 응답을 반환
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setData(List.of(Map.of("error", e.getMessage())));
            return response;
        }
    }




    private <T> Response<T> handleClientError(HttpClientErrorException e) {
        Response<T> response = new Response<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            Map<String, String> errorData = objectMapper.readValue(e.getResponseBodyAsString(), HashMap.class);
            response.setData((T) errorData);
        } catch (Exception parseException) {

            response.setData((T) e.getResponseBodyAsString());
            System.err.println("Failed to parse error response: " + parseException.getMessage());
        }

        response.setCode(e.getStatusCode());
        //System.err.println("Client error: " + e.getResponseBodyAsString());
        return response;
    }

}

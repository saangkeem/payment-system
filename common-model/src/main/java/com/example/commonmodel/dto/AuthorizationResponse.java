package com.example.commonmodel.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AuthorizationResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("refId")
    private String refId;

    @JsonProperty("tokenId")
    private String tokenId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("amount")
    private BigDecimal amount;

    // 기본 생성자
    public AuthorizationResponse() {
    }

    public AuthorizationResponse(String status, String message, String refId, String tokenId, String userId, BigDecimal amount) {
        this.status = status;
        this.message = message;
        this.refId = refId;
        this.tokenId = tokenId;
        this.userId = userId;
        this.amount = amount;
    }

    public AuthorizationResponse(String status, String message) {
        this.status = status;
        this.message = message;

    }

    // Getters and setters
}
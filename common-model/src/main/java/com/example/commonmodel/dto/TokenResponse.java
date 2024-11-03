package com.example.commonmodel.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TokenResponse {
    private String refId;
    private String tokenId;
    private String status;
    private String userId;
    private BigDecimal amount;

    public TokenResponse() {
    }

    public TokenResponse(String refId, String tokenId, String status, String userId, BigDecimal amount) {
        this.refId = refId;
        this.tokenId = tokenId;
        this.status = status;
        this.userId = userId;
        this.amount = amount;
    }

}
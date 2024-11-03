package com.example.commonmodel.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TokenRequest {
    private String userId;
    private String refId;
    private BigDecimal amount;

    // 기본 생성자 추가
    public TokenRequest() {
    }

    public TokenRequest(String userId,  String refId, BigDecimal amount) {
        this.userId = userId;
        this.refId = refId;
        this.amount = amount;
    }

    // Getters and setters
}

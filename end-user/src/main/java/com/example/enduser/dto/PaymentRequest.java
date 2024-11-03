package com.example.enduser.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class PaymentRequest {
    private String refId;
    private String userId;
    private BigDecimal amount;

    // 기본 생성자 추가
    public PaymentRequest() {
    }

    @JsonCreator
    public PaymentRequest(@JsonProperty("refId") String refId,
                          @JsonProperty("userId") String userId,
                          @JsonProperty("amount") BigDecimal amount) {
        this.refId = refId;
        this.userId = userId;
        this.amount = amount;
    }
}
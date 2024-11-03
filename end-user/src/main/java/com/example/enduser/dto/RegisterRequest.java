package com.example.enduser.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequest {
    private String ci;
    private String cardNumber;
    private String userId;

    // 기본 생성자 추가
    public RegisterRequest() {
    }

    @JsonCreator
    public RegisterRequest(@JsonProperty("cardNumber") String cardNumber,
                           @JsonProperty("ci") String ci,
                           @JsonProperty("userId") String userId) {
        this.ci = ci;
        this.cardNumber = cardNumber;
        this.userId = userId;
    }
}

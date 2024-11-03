package com.example.commonmodel.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardRegisterRequest {
    private String ci;
    private String encryptedCardNumber;
    private String cardHash;

    // 기본 생성자 추가
    public CardRegisterRequest() {
    }

    @JsonCreator
    public CardRegisterRequest(@JsonProperty("encryptedCardNumber") String encryptedCardNumber,
                               @JsonProperty("ci") String ci,
                               @JsonProperty("cardHash") String cardHash) {
        this.ci = ci;
        this.encryptedCardNumber = encryptedCardNumber;
        this.cardHash = cardHash;
    }
}


package com.example.commonmodel;

import java.util.UUID;

public class Token {
    private String id;
    private String cardNumber;

    // 생성자
    public Token(String cardNumber) {
        this.id = UUID.randomUUID().toString(); // 고유한 ID 생성
        this.cardNumber = cardNumber;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}

package com.example.commonmodel.exception;


public class CardInfoNotFoundException extends RuntimeException {
    public CardInfoNotFoundException(String message) {
        super(message);
    }
}
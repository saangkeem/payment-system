package com.example.commonmodel.exception;

public class CardAlreadyRegisteredException extends RuntimeException {
    public CardAlreadyRegisteredException(String message) {
        super(message);
    }
}
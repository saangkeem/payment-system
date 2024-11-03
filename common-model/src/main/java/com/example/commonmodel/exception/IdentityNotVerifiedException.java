package com.example.commonmodel.exception;

public class IdentityNotVerifiedException extends RuntimeException {
    public IdentityNotVerifiedException(String message) {
        super(message);
    }
}
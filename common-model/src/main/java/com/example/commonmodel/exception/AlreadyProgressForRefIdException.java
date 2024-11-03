package com.example.commonmodel.exception;

public class AlreadyProgressForRefIdException extends RuntimeException {
    public AlreadyProgressForRefIdException(String message) {
        super(message);
    }
}
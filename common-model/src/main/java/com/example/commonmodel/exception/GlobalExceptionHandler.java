package com.example.commonmodel.exception;


import com.example.commonmodel.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleCardAlreadyRegisteredException(CardAlreadyRegisteredException ex) {
        ErrorResponse errorResponse = new ErrorResponse("CARD_ALREADY_REGISTERED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse errorResponse = new ErrorResponse("ILLEGAL_STATE_ERROR", "An internal error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerErrorException(HttpServerErrorException ex) {
        ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", "Internal server error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // 기타 RuntimeException 처리
        ErrorResponse errorResponse = new ErrorResponse("RUNTIME_ERROR", "Runtime error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // 일반적인 예외 처리
        ErrorResponse errorResponse = new ErrorResponse("UNEXPECTED_ERROR", "An unexpected error occurred.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    @ExceptionHandler(CardInfoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardInfoNotFoundException(CardInfoNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("CARD_INFO_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    @ExceptionHandler(IdentityNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleIdentityNotVerifiedException(IdentityNotVerifiedException ex) {
        ErrorResponse errorResponse = new ErrorResponse("IDENTITY_VERIFY_ERORR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    @ExceptionHandler(AlreadyProgressForRefIdException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyProgressForRefIdException(AlreadyProgressForRefIdException ex) {
        ErrorResponse errorResponse = new ErrorResponse("ALREADY_PROGRESSED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }


}
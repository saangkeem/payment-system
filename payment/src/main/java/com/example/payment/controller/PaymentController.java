package com.example.payment.controller;

import com.example.commonmodel.dto.AuthorizationResponse;
import com.example.commonmodel.dto.CardRegisterRequest;
import com.example.commonmodel.dto.ErrorResponse;
import com.example.commonmodel.dto.TokenRequest;
import com.example.payment.model.Transaction;
import com.example.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public DeferredResult<AuthorizationResponse> initiatePayment(@RequestBody TokenRequest tokenRequest) {
        return paymentService.initiatePayment(tokenRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCard(@RequestBody CardRegisterRequest cardRegisterRequest) {
        String response = paymentService.registerCard(cardRegisterRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{refId}")
    public ResponseEntity<?> getTransaction(@PathVariable String refId) {
        List<Transaction> transaction = paymentService.getTransaction(refId);
        return ResponseEntity.ok(transaction);
    }
}

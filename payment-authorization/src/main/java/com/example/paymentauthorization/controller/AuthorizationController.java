package com.example.paymentauthorization.controller;

import com.example.commonmodel.dto.AuthorizationRequest;
import com.example.commonmodel.dto.AuthorizationResponse;
import com.example.paymentauthorization.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {

        this.authorizationService = authorizationService;

    }

    @PostMapping("/approve")
    public ResponseEntity<AuthorizationResponse> approvePayment(@RequestBody AuthorizationRequest authorizationRequest) {

        AuthorizationResponse response = authorizationService.verify(authorizationRequest);
        return ResponseEntity.ok(response);
    }

}

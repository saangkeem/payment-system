package com.example.enduser.controller;

import com.example.enduser.dto.PaymentRequest;
import com.example.enduser.dto.RegisterRequest;
import com.example.enduser.dto.Response;
import com.example.enduser.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;


@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/process-payment")
    public ResponseEntity<Response<?>> processPayment(@RequestBody PaymentRequest request) {
        Response<?> response  = userService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Response<?>> registerCard(@RequestBody RegisterRequest request) {
        Response<?> response  = userService.registerCard(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{refId}")
    public ResponseEntity<Response<?>> getTransaction(@PathVariable String refId) {
        Response<?> response = userService.getTransaction(refId);
        return ResponseEntity.ok(response);
    }

}
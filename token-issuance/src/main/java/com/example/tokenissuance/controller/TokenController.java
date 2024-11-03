package com.example.tokenissuance.controller;

import com.example.commonmodel.dto.AuthorizationRequest;
import com.example.commonmodel.dto.AuthorizationResponse;
import com.example.commonmodel.dto.CardRegisterRequest;
import com.example.tokenissuance.model.CardInfo;
import com.example.tokenissuance.service.TokenService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    @PostMapping("/register")
    public String registerCard(@RequestBody CardRegisterRequest cardRegisterRequest) {
        CardInfo cardInfo = tokenService.registerCard(cardRegisterRequest);
        return cardInfo.getRefId();  // REF_ID 반환
    }

    @GetMapping("/getCardInfo")
    public CardInfo getCardInfoByRefId(@RequestParam String refId) {
        return tokenService.getRefId(refId);
    }

    @GetMapping("/validateRefId")
    public String validateRefId(@RequestParam String refId) {
        CardInfo cardInfo = tokenService.getRefId(refId);
        return cardInfo.getRefId();
    }

    @PostMapping("/verify")
    public AuthorizationResponse verifyToken(@RequestBody AuthorizationRequest authorizationRequest) {
        AuthorizationResponse response = tokenService.verifyToken(authorizationRequest);
        return response;  // REF_ID 반환
    }

}

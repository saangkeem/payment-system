package com.example.commonmodel.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationRequest {
    private String refId;
    private String tokenId;

    // 기본 생성자 추가
    public AuthorizationRequest() {
    }

    public AuthorizationRequest(String refId, String tokenId) {
        this.refId = refId;
        this.tokenId = tokenId;
    }

}
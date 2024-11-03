package com.example.commonmodel.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardRegisterResponse {

    private String refId;
    private String status;

    public CardRegisterResponse() {
    }

    public CardRegisterResponse(String refId, String status) {

        this.refId = refId;
        this.status = status;
    }

}
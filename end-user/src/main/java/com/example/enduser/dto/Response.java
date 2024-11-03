package com.example.enduser.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
public class Response<T> {

    private HttpStatusCode code;
    private T data;

    public Response() {
    }

    public Response(HttpStatusCode code, T data) {
        this.code = code;
        this.data = data;
    }

}

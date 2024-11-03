package com.example.tokenissuance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.commonmodel", "com.example.tokenissuance"})
public class TokenIssuanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenIssuanceApplication.class, args);
    }

}

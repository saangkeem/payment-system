package com.example.paymentauthorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"com.example.commonmodel", "com.example.paymentauthorization"})
public class PaymentAuthorizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentAuthorizationApplication.class, args);
    }

}

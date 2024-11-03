package com.example.enduser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"com.example.commonmodel", "com.example.enduser"})
public class EndUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(EndUserApplication.class, args);
    }

}

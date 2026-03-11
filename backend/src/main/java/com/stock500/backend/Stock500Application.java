package com.stock500.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Stock500Application {
    public static void main(String[] args) {
        SpringApplication.run(Stock500Application.class, args);
    }
}

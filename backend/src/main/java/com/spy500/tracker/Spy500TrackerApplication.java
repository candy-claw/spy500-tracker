package com.spy500.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Spy500TrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(Spy500TrackerApplication.class, args);
    }
}

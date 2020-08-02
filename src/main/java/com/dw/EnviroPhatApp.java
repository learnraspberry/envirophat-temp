package com.dw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EnviroPhatApp {
    public static void main(String[] args) {
        SpringApplication.run(EnviroPhatApp.class, args);
    }
}

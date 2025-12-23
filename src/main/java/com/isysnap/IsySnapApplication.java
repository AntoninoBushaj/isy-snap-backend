package com.isysnap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IsySnapApplication {

    public static void main(String[] args) {
        SpringApplication.run(IsySnapApplication.class, args);
    }

}
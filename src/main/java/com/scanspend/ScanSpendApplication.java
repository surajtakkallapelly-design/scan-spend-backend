package com.scanspend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScanSpendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScanSpendApplication.class, args);
    }
}

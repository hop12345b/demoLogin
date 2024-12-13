package com.example.demo;

import com.example.demo.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppMain {
    private static final Logger logger = LoggerFactory.getLogger(AppMain.class);

    public static void main(String[] args) {
        SpringApplication.run(AppMain.class, args);
    }
}

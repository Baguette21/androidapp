package com.delacruz.trivia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TriviaGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(TriviaGameApplication.class, args);
    }
}

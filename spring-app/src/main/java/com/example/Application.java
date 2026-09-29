package com.example;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.checks.SmokeChecks;

/**
 * Runs the smoke checks once the application has started. The package
 * com.example is the auto-configuration package, so com.example.checks is
 * inside it and org.example.addon is outside it.
 */
@SpringBootApplication
public class Application implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        int failed = SmokeChecks.runAndPrint("Spring Boot");
        if (!args.containsOption("keep-running")) {
            System.exit(failed == 0 ? 0 : 1);
        }
    }
}

package com.example;

import java.util.List;

import jakarta.servlet.ServletContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.checks.SmokeChecks;
import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

/**
 * Runs the smoke checks once the application has started. The package
 * com.example is the auto-configuration package, so com.example.checks is
 * inside it and org.example.addon is outside it.
 */
@SpringBootApplication
public class Application implements ApplicationRunner {

    private final ServletContext servletContext;

    public Application(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        int failed = SmokeChecks.runAndPrint("Spring Boot",
                List.of(new SmokeChecks.Extra("4a",
                        "vaadin.* property from application.properties",
                        this::checkVaadinProperty)));
        if (!args.containsOption("keep-running")) {
            System.exit(failed == 0 ? 0 : 1);
        }
    }

    /**
     * SpringApplicationConfigurationFactory copies the vaadin.* properties
     * whose names SpringServlet.PROPERTY_NAMES lists. That list is built with
     * InitParameters.class.getDeclaredFields().
     */
    private String checkVaadinProperty() {
        ApplicationConfiguration config = ApplicationConfiguration
                .get(new VaadinServletContext(servletContext));
        String value = config.getStringProperty(
                InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, null);
        return "123".equals(value) ? null
                : "vaadin.heartbeatInterval=123 was not read (got '" + value
                        + "')";
    }
}

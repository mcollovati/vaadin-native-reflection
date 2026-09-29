package com.example;

import java.util.List;

import jakarta.servlet.ServletContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.example.checks.SmokeChecks;
import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.flow.server.startup.ApplicationConfigurationFactory;
import com.vaadin.flow.spring.SpringServlet;

/**
 * Runs the smoke checks once the application has started. The package
 * com.example is the auto-configuration package, so com.example.checks is
 * inside it and org.example.addon is outside it.
 */
@SpringBootApplication
public class Application implements ApplicationRunner {

    private final ServletContext servletContext;
    private final Environment environment;
    private final ApplicationContext applicationContext;

    public Application(ServletContext servletContext,
            ApplicationContext applicationContext,
            Environment environment) {
        this.servletContext = servletContext;
        this.environment = environment;
        this.applicationContext = applicationContext;
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
        VaadinServletContext context = new VaadinServletContext(
                servletContext);
        ApplicationConfiguration config = ApplicationConfiguration
                .get(context);
        String value = config.getStringProperty(
                InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, null);
        if ("123".equals(value)) {
            return null;
        }
        // Tells which step lost the value
        Lookup lookup = context.getAttribute(Lookup.class);
        ApplicationConfigurationFactory factory = lookup == null ? null
                : lookup.lookup(ApplicationConfigurationFactory.class);
        List<String> names = PropertyNames.get();
        return "vaadin.heartbeatInterval=123 was not read (got '" + value
                + "'). Spring environment: '"
                + environment.getProperty("vaadin.heartbeatInterval")
                + "', SpringServlet.PROPERTY_NAMES: " + names.size()
                + " names, contains heartbeatInterval: "
                + names.contains(
                        InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL)
                + ", InitParameters.getDeclaredFields(): "
                + InitParameters.class.getDeclaredFields().length
                + ", configuration factory: "
                + (factory == null ? null : factory.getClass().getName())
                + ", configuration: " + config.getClass().getName()
                + ", lookup: "
                + (lookup == null ? null : lookup.getClass().getName())
                + ", factory beans in the application context: "
                + List.of(applicationContext.getBeanNamesForType(
                        ApplicationConfigurationFactory.class))
                + ", in the servlet context's web application context: "
                + webContextFactoryBeans();
    }

    private Object webContextFactoryBeans() {
        WebApplicationContext webContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContext);
        return webContext == null ? "no web application context"
                : List.of(webContext.getBeanNamesForType(
                        ApplicationConfigurationFactory.class))
                        + (webContext == applicationContext ? " (same context)"
                                : " (other context)");
    }

    /** Only here to read the protected SpringServlet.PROPERTY_NAMES. */
    private static final class PropertyNames extends SpringServlet {

        private PropertyNames(ApplicationContext context) {
            super(context, false);
        }

        static List<String> get() {
            return PROPERTY_NAMES;
        }
    }
}

package com.example.quarkus;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.checks.SmokeChecks;

/** Runs the smoke checks once the application has started. */
@ApplicationScoped
public class SmokeRunner {

    @ConfigProperty(name = "smoke.keep-running", defaultValue = "false")
    boolean keepRunning;

    void onStart(@Observes StartupEvent event) {
        int failed = SmokeChecks.runAndPrint("Quarkus");
        if (!keepRunning) {
            Quarkus.asyncExit(failed == 0 ? 0 : 1);
        }
    }
}

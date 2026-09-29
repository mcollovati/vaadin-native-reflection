package com.example.checks;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/** Same shape as AddonEvent, but in the application package. */
@DomEvent("app-event")
public class AppEvent extends ComponentEvent<AppWidget> {

    private final String detail;

    public AppEvent(AppWidget source, boolean fromClient,
            @EventData("event.detail") String detail) {
        super(source, fromClient);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }
}

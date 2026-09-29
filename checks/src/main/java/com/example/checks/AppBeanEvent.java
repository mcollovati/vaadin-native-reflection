package com.example.checks;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/** Event whose @EventData is a bean, not a simple type. */
@DomEvent("app-bean-event")
public class AppBeanEvent extends ComponentEvent<AppWidget> {

    private final EventDetailBean detail;

    public AppBeanEvent(AppWidget source, boolean fromClient,
            @EventData("event.detail") EventDetailBean detail) {
        super(source, fromClient);
        this.detail = detail;
    }

    public EventDetailBean getDetail() {
        return detail;
    }
}

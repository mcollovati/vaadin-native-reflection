package org.example.addon;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Same shape as the application's AppEvent. ComponentEventBus creates it with
 * reflection when the DOM event arrives.
 */
@DomEvent("addon-event")
public class AddonEvent extends ComponentEvent<AddonWidget> {

    private final String detail;

    public AddonEvent(AddonWidget source, boolean fromClient,
            @EventData("event.detail") String detail) {
        super(source, fromClient);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }
}

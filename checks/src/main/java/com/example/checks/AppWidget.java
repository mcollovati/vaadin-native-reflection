package com.example.checks;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.shared.Registration;

/** A component in the application package. */
@Tag("app-widget")
public class AppWidget extends Component {

    public Registration addAppEventListener(
            ComponentEventListener<AppEvent> listener) {
        return addListener(AppEvent.class, listener);
    }

    public Registration addAppBeanEventListener(
            ComponentEventListener<AppBeanEvent> listener) {
        return addListener(AppBeanEvent.class, listener);
    }

    /**
     * Only here so that the native integrations register CallableArgBean
     * (the control check).
     */
    @ClientCallable
    void receive(CallableArgBean arg) {
    }
}

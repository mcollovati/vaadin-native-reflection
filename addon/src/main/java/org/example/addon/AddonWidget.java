package org.example.addon;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.shared.Registration;

/**
 * A component from an add-on, in a package outside the application package.
 */
@Tag("addon-widget")
public class AddonWidget extends Component {

    public Registration addAddonEventListener(
            ComponentEventListener<AddonEvent> listener) {
        return addListener(AddonEvent.class, listener);
    }
}

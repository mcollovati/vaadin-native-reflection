package com.example.checks;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Tag;

/** Minimal HasValue field for the Binder checks. */
@Tag("input")
public class TextInput extends AbstractSinglePropertyField<TextInput, String> {

    public TextInput() {
        super("value", "", false);
    }
}

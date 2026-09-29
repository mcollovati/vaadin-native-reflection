package org.example.addon;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

/**
 * A converter from an add-on, used only as a reflection registration probe.
 */
public class AddonConverter implements Converter<String, Integer> {

    @Override
    public Result<Integer> convertToModel(String value, ValueContext context) {
        return Result.ok(Integer.valueOf(value));
    }

    @Override
    public String convertToPresentation(Integer value, ValueContext context) {
        return String.valueOf(value);
    }
}

package com.example.checks;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

/**
 * A converter in the application package, used only as a reflection
 * registration probe.
 */
public class AppConverter implements Converter<String, Integer> {

    @Override
    public Result<Integer> convertToModel(String value, ValueContext context) {
        return Result.ok(Integer.valueOf(value));
    }

    @Override
    public String convertToPresentation(Integer value, ValueContext context) {
        return String.valueOf(value);
    }
}

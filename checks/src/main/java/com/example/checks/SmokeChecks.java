package com.example.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.example.addon.AddonConverter;
import org.example.addon.AddonEvent;
import org.example.addon.AddonWidget;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.BeanDataGenerator;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonCodec;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Calls the Flow code paths that use reflection on application classes, and
 * reports for each one whether it worked.
 * <p>
 * The checks call the same Flow code that runs when a browser sends a request,
 * but without a browser: for example a DOM event is fired directly on the
 * element's listener map. Every check uses its own bean class, so that a
 * registration for one check cannot hide a failure in another.
 */
public final class SmokeChecks {

    /** The result of one check. */
    public record Result(String id, String feature, boolean control,
            boolean passed, String detail) {
    }

    @FunctionalInterface
    private interface Check {
        /** Returns null when the check passed, or a failure message. */
        String run() throws Exception;
    }

    private final List<Result> results = new ArrayList<>();

    private SmokeChecks() {
    }

    /**
     * Runs all checks.
     *
     * @return the results, in a fixed order
     */
    public static List<Result> runAll() {
        SmokeChecks checks = new SmokeChecks();
        checks.runChecks();
        return checks.results;
    }

    /**
     * Prints the results as a table and returns the number of failed checks.
     *
     * @param runtime
     *            a label for the runtime, printed in the header
     * @return the number of failed checks
     */
    public static int runAndPrint(String runtime) {
        List<Result> results = runAll();
        boolean nativeImage = System
                .getProperty("org.graalvm.nativeimage.imagecode") != null;
        StringBuilder out = new StringBuilder();
        out.append("\n==== Flow native reflection smoke test: ")
                .append(runtime).append(nativeImage ? " (native)" : " (JVM)")
                .append(" ====\n");
        int failed = 0;
        for (Result r : results) {
            if (!r.passed()) {
                failed++;
            }
            out.append(String.format("%-4s %-4s %-58s%s%n",
                    r.passed() ? "PASS" : "FAIL", r.id(),
                    r.feature() + (r.control() ? " [control]" : ""),
                    r.passed() ? "" : " -> " + r.detail()));
        }
        out.append(String.format("==== %d of %d checks failed ====%n", failed,
                results.size()));
        System.out.println(out);
        return failed;
    }

    private void runChecks() {
        // Controls: these types get native hints today, so they must pass.
        check("C1", "@ClientCallable argument decode", true, () -> {
            CallableArgBean value = JacksonCodec.decodeAs(json("callable"),
                    CallableArgBean.class);
            return expect("callable", value.getName());
        });
        check("C2", "@DomEvent in app package (String @EventData)", true,
                () -> {
                    AppWidget widget = new AppWidget();
                    AtomicReference<String> got = new AtomicReference<>();
                    widget.addAppEventListener(e -> got.set(e.getDetail()));
                    fireDomEvent(widget.getElement(), "app-event",
                            JacksonUtils.getMapper().valueToTree("hello"));
                    return expect("hello", got.get());
                });

        // Section 1: application data classes that Flow reads with
        // reflection, and that no native integration registers.
        check("1a", "Binder(BinderBean.class).bind(\"name\")", false, () -> {
            Binder<BinderBean> binder = new Binder<>(BinderBean.class);
            TextInput input = new TextInput();
            binder.forField(input).bind("name");
            binder.readBean(new BinderBean("binder"));
            return expect("binder", input.getValue());
        });
        check("1b", "Binder(BinderRecord.class).writeRecord()", false, () -> {
            Binder<BinderRecord> binder = new Binder<>(BinderRecord.class);
            TextInput input = new TextInput();
            binder.forField(input).bind("name");
            input.setValue("record");
            return expect("record", binder.writeRecord().name());
        });
        check("1c", "Element.setPropertyBean(...)", false, () -> {
            Element element = new Element("div");
            element.setPropertyBean("p", new PropertyBean("property"));
            Object raw = element.getPropertyRaw("p");
            return raw instanceof ObjectNode node && node.has("name")
                    ? expect("property", node.get("name").asString())
                    : "property JSON was " + raw;
        });
        check("1d", "executeJs(...) bean argument encode", false, () -> {
            JsonNode encoded = JacksonCodec
                    .encodeWithTypeInfo(new JsArgBean("jsarg"));
            return encoded.has("name")
                    ? expect("jsarg", encoded.get("name").asString())
                    : "encoded JSON was " + encoded;
        });
        check("1e", "PendingJavaScriptResult.then(JsResultBean.class)", false,
                () -> {
                    JsResultBean value = JacksonCodec.decodeAs(json("jsresult"),
                            JsResultBean.class);
                    return expect("jsresult", value.getName());
                });
        check("1f", "@DomEvent with bean @EventData", false, () -> {
            AppWidget widget = new AppWidget();
            AtomicReference<EventDetailBean> got = new AtomicReference<>();
            widget.addAppBeanEventListener(e -> got.set(e.getDetail()));
            fireDomEvent(widget.getElement(), "app-bean-event",
                    json("eventdetail"));
            return got.get() == null ? "listener got no event"
                    : expect("eventdetail", got.get().getName());
        });
        check("1g", "SharedValueSignal<SignalBean> set/peek", false, () -> {
            SharedValueSignal<SignalBean> signal = new SharedValueSignal<>(
                    SignalBean.class);
            signal.set(new SignalBean("signal"));
            SignalBean value = signal.peek();
            return value == null ? "peek() returned null"
                    : expect("signal", value.getName());
        });
        check("1h", "Trigger CallbackAction value decode (readValue)", false,
                () -> {
                    CallbackBean value = JacksonUtils
                            .readValue(json("callback"), CallbackBean.class);
                    return expect("callback", value.getName());
                });
        check("1i", "BeanDataGenerator.generateData(...)", false, () -> {
            ObjectNode data = JacksonUtils.createObjectNode();
            new BeanDataGenerator<GridItemBean>()
                    .generateData(new GridItemBean("griditem"), data);
            return data.has("name")
                    ? expect("griditem", data.get("name").asString())
                    : "generated data was " + data;
        });

        // Section 2: classes outside the Spring Boot application package.
        check("2a", "@DomEvent in add-on package (String @EventData)", false,
                () -> {
                    AddonWidget widget = new AddonWidget();
                    AtomicReference<String> got = new AtomicReference<>();
                    widget.addAddonEventListener(e -> got.set(e.getDetail()));
                    fireDomEvent(widget.getElement(), "addon-event",
                            JacksonUtils.getMapper().valueToTree("hello"));
                    return expect("hello", got.get());
                });
        check("2b", "Probe: AddonConverter is registered", false,
                () -> probeConstructors(AddonConverter.class));

        // Section 3: Quarkus looks up interface implementations with
        // getAllKnownSubclasses(). No Flow feature creates a Converter with
        // reflection today, so this is only a registration probe.
        check("3a", "Probe: AppConverter (implements Converter) registered",
                false, () -> probeConstructors(AppConverter.class));
    }

    private void check(String id, String feature, boolean control,
            Check check) {
        String failure;
        try {
            failure = check.run();
        } catch (Throwable t) {
            failure = describe(t);
        }
        results.add(new Result(id, feature, control, failure == null,
                failure));
    }

    private static String expect(String expected, Object actual) {
        return expected.equals(actual) ? null
                : "expected '" + expected + "' but got '" + actual + "'";
    }

    private static String probeConstructors(Class<?> type) {
        int count = type.getDeclaredConstructors().length;
        return count > 0 ? null
                : "getDeclaredConstructors() is empty (not registered)";
    }

    private static ObjectNode json(String name) {
        ObjectNode node = JacksonUtils.createObjectNode();
        node.put("name", name);
        return node;
    }

    private static void fireDomEvent(Element element, String type,
            JsonNode detail) {
        ObjectNode eventData = JacksonUtils.createObjectNode();
        eventData.set("event.detail", detail);
        element.getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(element, type, eventData));
    }

    private static String describe(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable current = t;
        while (current != null) {
            if (!sb.isEmpty()) {
                sb.append(" <- ");
            }
            sb.append(current.getClass().getSimpleName()).append(": ")
                    .append(firstLine(current.getMessage()));
            current = current.getCause() == current ? null
                    : current.getCause();
        }
        return sb.toString();
    }

    private static String firstLine(String message) {
        if (message == null) {
            return "";
        }
        String line = message.lines().findFirst().orElse("");
        return line.length() > 160 ? line.substring(0, 160) + "..." : line;
    }
}

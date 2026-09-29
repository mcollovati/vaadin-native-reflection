# Flow native reflection smoke test

Checks the Flow code paths that use reflection on application classes, in a
Spring Boot and a Quarkus native image. The checks run at startup, print a
PASS/FAIL table and exit (exit code 1 when a check fails).

- `checks/`: the checks (`SmokeChecks`) and the "application" classes, in
  `com.example.checks` (inside the Spring Boot application package, with a
  Jandex index).
- `addon/`: an add-on in `org.example.addon` (outside the application
  package, no Jandex index).
- `spring-app/`, `quarkus-app/`: run the checks at startup.

The checks call Flow directly, without a browser. For example, a DOM event is
fired on the element's listener map, and `then(Class)` is checked through
`JacksonCodec.decodeAs`, which is the code it uses. Each check has its own
bean class, so that the registration for one check cannot hide a failure in
another.

| Id | What | Spring native | Quarkus native |
|----|------|---------------|----------------|
| C1, C2 | Controls: `@ClientCallable` argument, app-package `@DomEvent` | PASS | PASS |
| 1a-1i | Binder, `setPropertyBean`, `executeJs` args, `then(Class)`, bean `@EventData`, shared signals, trigger values, `BeanDataGenerator` | FAIL | FAIL |
| 2a, 2b | Add-on event and converter outside the app package (Spring: vaadin/flow#26030; Quarkus: the add-on jar has no Jandex index) | FAIL | FAIL |
| 3a | Probe: `Converter` implementation registered (Quarkus: vaadin/flow#26031) | PASS | FAIL |
| 4a | Spring only: a `vaadin.*` property from `application.properties` reaches Flow (`SpringServlet.PROPERTY_NAMES` uses `InitParameters.class.getDeclaredFields()`) | FAIL | - |

Results with GraalVM CE 25.2.4 (JDK 25.0.4), Flow 25.4-SNAPSHOT of
2026-09-29, Spring Boot 4.1.1 and Quarkus 3.33.0. On the JVM, all checks
pass.

## Run

JVM (all checks must pass):

    mvn install -N && mvn install -pl addon,checks
    (cd spring-app && mvn package && java -jar target/spring-app-1.0-SNAPSHOT.jar)
    (cd quarkus-app && mvn package && java -jar target/quarkus-app/quarkus-run.jar)

Native (needs Mandrel/GraalVM 25, gcc and zlib headers):

    ./run-native.sh            # or: ./run-native.sh spring | quarkus

Results are written to `results/`: `*-native.txt` has the table, and
`*-build.log` and `*-run.log` have the full build and run output.

## Notes

- Flow `25.4-SNAPSHOT` comes from the Vaadin prerelease repository. Use
  `-Dflow.version=...` to test another version, for example a local build.
- `run-native.sh` uses the Java in `JAVA_HOME`, and stops at once when it is
  not a GraalVM or Mandrel installation (no `bin/native-image`).
- The Quarkus app uses Quarkus 3.33.0: the native build with 3.33.4
  fails with `NoClassDefFoundError:
  com/fasterxml/jackson/core/util/ByteArrayBuilder`.

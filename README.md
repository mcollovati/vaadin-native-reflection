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

| Id | What | Expected in native |
|----|------|--------------------|
| C1, C2 | Controls: `@ClientCallable` argument, app-package `@DomEvent` | PASS (hints exist) |
| 1a-1i | Binder, `setPropertyBean`, `executeJs` args, `then(Class)`, bean `@EventData`, shared signals, trigger values, `BeanDataGenerator` | FAIL (no hints) |
| 2a, 2b | Add-on event and converter outside the app package | FAIL on Spring (vaadin/flow#26030); Quarkus depends on indexing |
| 3a | Probe: `Converter` implementation registered | FAIL on Quarkus (vaadin/flow#26031), PASS on Spring |

## Run

JVM (all checks must pass):

    mvn install -N && mvn install -pl addon,checks
    (cd spring-app && mvn package && java -jar target/spring-app-1.0-SNAPSHOT.jar)
    (cd quarkus-app && mvn package && java -jar target/quarkus-app/quarkus-run.jar)

Native (needs Mandrel/GraalVM 25, gcc and zlib headers):

    ./run-native.sh            # or: ./run-native.sh spring | quarkus

Results are written to `results/`.

## Notes

- Flow `25.4-SNAPSHOT` comes from the Vaadin prerelease repository. Use
  `-Dflow.version=...` to test another version, for example a local build.
- The "Expected in native" column is what the code analysis predicts. It is
  not yet confirmed by a native run.
- `run-native.sh` uses Mandrel from `$HOME/.sdkman`; set `MANDREL` to use
  another GraalVM or Mandrel installation.

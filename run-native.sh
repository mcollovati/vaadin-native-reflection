#!/usr/bin/env bash
# Builds the Spring Boot and Quarkus apps as native images and runs the
# smoke checks in each. Needs gcc and the zlib headers (build-essential,
# zlib1g-dev on Ubuntu). Output goes to results/*.txt.
#
# Usage: ./run-native.sh [spring|quarkus|all]   (default: all)
set -u
cd "$(dirname "$0")"
MANDREL=${MANDREL:-$HOME/.sdkman/candidates/java/25.0.2.r25-mandrel}
export JAVA_HOME=$MANDREL GRAALVM_HOME=$MANDREL
what=${1:-all}
mkdir -p results

mvn -q install -N && mvn -q install -pl addon,checks || exit 1

if [[ $what == all || $what == spring ]]; then
    echo ">>> Spring Boot native build (log: results/spring-build.log)"
    (cd spring-app && mvn -Pnative native:compile -DskipTests) \
        > results/spring-build.log 2>&1 \
        && ./spring-app/target/spring-app 2>&1 \
            | sed -n '/==== Flow native/,/checks failed ====/p' \
            | tee results/spring-native.txt \
        || echo "Spring build or run failed, see results/spring-build.log"
fi

if [[ $what == all || $what == quarkus ]]; then
    echo ">>> Quarkus native build (log: results/quarkus-build.log)"
    (cd quarkus-app && mvn package -DskipTests -Dquarkus.native.enabled=true) \
        > results/quarkus-build.log 2>&1 \
        && ./quarkus-app/target/quarkus-app-1.0-SNAPSHOT-runner 2>&1 \
            | sed -n '/==== Flow native/,/checks failed ====/p' \
            | tee results/quarkus-native.txt \
        || echo "Quarkus build or run failed, see results/quarkus-build.log"
fi

#!/usr/bin/env bash
# Builds the Spring Boot and Quarkus apps as native images and runs the
# smoke checks in each. Needs gcc and the zlib headers (build-essential,
# zlib1g-dev on Ubuntu), and JAVA_HOME must point to a GraalVM or Mandrel
# installation. Output goes to results/*.txt.
#
# Usage: ./run-native.sh [spring|quarkus|all]   (default: all)
set -u
cd "$(dirname "$0")"

if [[ -z ${JAVA_HOME:-} ]]; then
    echo "JAVA_HOME is not set. Set it to a GraalVM or Mandrel installation." >&2
    exit 1
fi
if [[ ! -x $JAVA_HOME/bin/native-image ]]; then
    echo "JAVA_HOME ($JAVA_HOME) is not a GraalVM or Mandrel installation:" \
        "$JAVA_HOME/bin/native-image not found." >&2
    exit 1
fi
export GRAALVM_HOME=$JAVA_HOME
echo ">>> Using $("$JAVA_HOME/bin/native-image" --version | head -1)"

what=${1:-all}
mkdir -p results

mvn -q install -N && mvn -q install -pl addon,checks || exit 1

# Builds one app and runs its native binary.
#   $1: name used for the files in results/
#   $2: app directory
#   $3: path of the native binary
#   rest: Maven arguments for the native build
build_and_run() {
    local name=$1 dir=$2 binary=$3
    shift 3
    echo ">>> $name native build (log: results/$name-build.log)"
    if ! (cd "$dir" && mvn "$@") > "results/$name-build.log" 2>&1; then
        echo "!!! $name native build failed, see results/$name-build.log"
        return
    fi
    echo ">>> Running $binary (log: results/$name-run.log)"
    "./$binary" > "results/$name-run.log" 2>&1
    sed -n '/==== Flow native/,/checks failed ====/p' "results/$name-run.log" \
        > "results/$name-native.txt"
    if [[ -s results/$name-native.txt ]]; then
        cat "results/$name-native.txt"
    else
        echo "!!! $name did not run the checks, see results/$name-run.log"
    fi
}

if [[ $what == all || $what == spring ]]; then
    build_and_run spring spring-app spring-app/target/spring-app \
        -Pnative native:compile -DskipTests
fi

if [[ $what == all || $what == quarkus ]]; then
    build_and_run quarkus quarkus-app \
        quarkus-app/target/quarkus-app-1.0-SNAPSHOT-runner \
        package -DskipTests -Dquarkus.native.enabled=true
fi

#!/usr/bin/env sh
set -eu

GRADLE_HOME="${GRADLE_HOME:-$HOME/.gradle/wrapper/dists/gradle-9.1.0-bin/9agqghryom9wkf8r80qlhnts3/gradle-9.1.0}"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  echo "Gradle 9.1.0 is not available at $GRADLE_HOME" >&2
  echo "Open the project in Android Studio or install/configure Gradle before running this script." >&2
  exit 1
fi

exec "$GRADLE_HOME/bin/gradle" "$@"

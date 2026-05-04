#!/usr/bin/env sh

# ----------------------------------------------------------------------------
# Maven Wrapper
# ----------------------------------------------------------------------------

PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")"/$link"
  fi
 done

MAVEN_HOME=
PRGDIR=$(dirname "$PRG")
THE_BASE_DIR=$(cd "$PRGDIR" >/dev/null && pwd)

if [ -z "$MAVEN_HOME" ]; then
  MAVEN_HOME="$THE_BASE_DIR/.mvn/wrapper"
fi

exec "$JAVA_HOME/bin/java" -cp "$MAVEN_HOME/maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain "$@"

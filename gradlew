#!/bin/sh
# Gradle start up script for POSIX systems
APP_HOME=$(cd "${0%/*}" && pwd -P)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
MAX_FD="maximum"
warn () { echo "$*"; } >&2
die () { echo; echo "$*"; echo; exit 1; } >&2
cygwin=false
msys=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  MSYS* | MINGW* ) msys=true ;;
  NonStop* ) nonstop=true ;;
esac
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD=java
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    case $MAX_FD in
      max*)
        MAX_FD=$(ulimit -H -n) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in
      '' | soft) :;;
      *) ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi
if [ "$darwin" = "false" -a "$msys" = "false" ] ; then
    case "$(set -- 2>/dev/null; echo $?)" in
        0) ;;
        *) warn "Unsupported shell, your $SHELL may not work with this script." ;;
    esac
fi
if [ "$cygwin" = "false" -a "$msys" = "false" ] ; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi
if [ "$cygwin" = "false" -a "$msys" = "false" -a "$nonstop" = "false" ] ; then
    case $GRADLE_OPTS in
        *-Dorg.gradle.jvmargs=*) ;;
        *) GRADLE_OPTS="$GRADLE_OPTS \"-Dorg.gradle.jvmargs=-Xmx64m\"" ;;
    esac
fi
save () {
    for i do printf %s\\n "$i" | sed "s/'/'\\\\''/g;1s/^/'/;\$s/\$/' \\\\/" ; done
    echo " "
}
APP_ARGS=$(save "$@")
eval set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" -classpath "\"$CLASSPATH\"" org.gradle.wrapper.GradleWrapperMain "$APP_ARGS"
exec "$JAVACMD" "$@"
#!/bin/sh
# Script de inicio de Gradle para sistemas POSIX (Termux / Linux / GitHub Actions)

APP_HOME=$(cd "${0%/*}" && pwd -P)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# OPTIMIZACIÓN: Inyección directa de 3GB de RAM máximos en la JVM base de Termux
DEFAULT_JVM_OPTS='"-Xmx3g" "-Xms512m" "-XX:MaxMetaspaceSize=512m"'
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

# Verificación y enlace con el entorno de ejecución de Java (JDK 17+)
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME apunta a un directorio invalido: $JAVA_HOME"
    fi
else
    JAVACMD=java
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME no esta configurado y no se encontro el comando 'java' en tu PATH."
fi

# Optimización de Descriptores de Archivos máximos (File Descriptors) para compilación paralela
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    case $MAX_FD in
      max*)
        MAX_FD=$(ulimit -H -n) ||
            warn "No se pudo consultar el limite maximo de descriptores de archivos"
    esac
    case $MAX_FD in
      '' | soft) :;;
      *) ulimit -n "$MAX_FD" ||
            warn "No se pudo establecer el limite maximo de descriptores de archivos a $MAX_FD"
    esac
fi

if [ "$darwin" = "false" -a "$msys" = "false" ] ; then
    case "$(set -- 2>/dev/null; echo $?)" in
        0) ;;
        *) warn "Shell no soportado de forma nativa, su entorno $SHELL podria presentar alertas." ;;
    esac
fi

# OPTIMIZACIÓN: Forzar 3GB de RAM máximos también en las opciones por defecto de Gradle si vienen vacías
if [ "$cygwin" = "false" -a "$msys" = "false" -a "$nonstop" = "false" ] ; then
    case $GRADLE_OPTS in
        *-Dorg.gradle.jvmargs=*) ;;
        *) GRADLE_OPTS="$GRADLE_OPTS \"-Dorg.gradle.jvmargs=-Xmx3g\"" ;;
    esac
fi

save () {
    for i do printf %s\\n "$i" | sed "s/'/'\\\\''/g;1s/^/'/;\$s/\$/' \\\\/" ; done
    echo " "
}

APP_ARGS=$(save "$@")

# Enlace de argumentos finales del sistema de construcción de HyperNexus
eval set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" -classpath "\"$CLASSPATH\"" org.gradle.wrapper.GradleWrapperMain "$APP_ARGS"

exec "$JAVACMD" "$@"

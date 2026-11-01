@rem Script de inicio de Gradle para entornos de desarrollo en Windows (HyperNexus)
@if "%DEBUG%"=="" @echo off

@rem Configurar el alcance local de variables si se usa la terminal NT de Windows
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem OPTIMIZACIÓN: Inyección de 3GB de RAM máximos para el compilador en Windows en lugar de los 64MB de fábrica
set DEFAULT_JVM_OPTS="-Xmx3g" "-Xms512m" "-XX:MaxMetaspaceSize=512m"

if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME no esta configurado y no se encontro el comando 'java' en tu PATH. 1>&2
echo. 1>&2
echo Por favor, configura la variable de entorno JAVA_HOME para que apunte a la 1>&2
echo ubicacion de tu instalacion de Java (Se requiere JDK 17 o superior). 1>&2
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME apunta a un directorio invalido: %JAVA_HOME% 1>&2
echo. 1>&2
echo Por favor, corrige la variable de entorno JAVA_HOME para que coincida con la 1>&2
echo ubicacion de tu instalacion de Java. 1>&2
goto fail

:execute
@rem Configurar el puente hacia el ejecutable JAR del Wrapper de Gradle
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

@rem Ejecutar el proceso principal con las optimizaciones de memoria añadidas
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Salida en caso de error en el entorno de compilacion
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega

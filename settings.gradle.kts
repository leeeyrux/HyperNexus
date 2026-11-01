pluginManagement {
    // Repositorios donde Gradle buscará los plugins del sistema de construcción
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Forzar a que todas las dependencias del proyecto se definan centralizadamente aquí
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    
    // Repositorios globales para las librerías de la app (Compose, Shizuku, Material 3, etc.)
    repositories {
        google()           // Repositorio principal de Android y Jetpack Compose
        mavenCentral()     // Repositorio estándar para librerías de código abierto de Java/Kotlin
    }
}

// Nombre de la aplicación raíz en el espacio de trabajo
rootProject.name = "HyperNexus"

// Módulos incluidos en la compilación de la APK
include(":app")

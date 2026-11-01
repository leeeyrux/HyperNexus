// Top-level build file: Configuración global de plugins y compilación

plugins {
    // Plugin de aplicaciones Android estable y compatible
    id("com.android.application") version "8.2.0" apply false
    // Plugin de Kotlin indispensable para que el compilador entienda las tareas de compilación
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}

// Configuración global para todas las tareas de compilación de Kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        // Forzar el uso de Java 17 de forma global en todo el entorno
        jvmTarget = "17"
        
        // Habilitar de forma masiva las APIs experimentales de Material 3
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}

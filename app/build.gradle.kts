// Build file interno: Configuración de compilación, empaquetado y dependencias del módulo :app

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hypernexus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hypernexus"
        minSdk = 26          // Compatible desde Android 8.0 (Oreo) en adelante
        targetSdk = 34       // Apuntando a Android 14 (Upside Down Cake)
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            // Optimización extrema (R8): Remoción de código muerto y recursos huérfanos
            isMinifyEnabled = true
            isShrinkResources = true

            // BLINDAJE DE INSTALACIÓN: Firmar automáticamente el APK de Release con la llave debug para poder probarlo en Termux
            signingConfig = signingConfigs.getByName("debug")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Forzar compatibilidad nativa con Java 17 para el motor de Android
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // Enlazar el compilador de Kotlin al bytecode de Java 17
        jvmTarget = "17"
    }

    buildFeatures {
        // Habilitar el compilador y empaquetador de Jetpack Compose
        compose = true
    }

    composeOptions {
        // Versión fija del compilador de Compose emparejada estrictamente con Kotlin 1.9.20
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            // Excluir archivos de licencias duplicados para evitar colisiones de empaquetado en el APK
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // Luz verde en Linters: Evita que alertas visuales o estéticas detengan la compilación en Termux
        abortOnError = false
        checkReleaseBuilds = false
    }
}
dependencies {
    // 1. Núcleo e Interfaz Gráfica (Jetpack Compose BOM de Octubre 2023)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // 2. Puentes de Sistema y Privilegios Elevados (Shizuku API)
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")

    // 3. Ciclo de Vida y Arquitectura de Componentes
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")

    // 4. Procesamiento Asíncrono y Concurrencia (Corrutinas de Kotlin)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // 5. Motor de Persistencia de Datos (Almacenamiento Local de Preferencias)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // 6. Herramientas de Depuración en Tiempo de Diseño
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

# =============================================================================
# REGLAS DE OPTIMIZACIÓN Y ESTABILIDAD PARA HYPERNEXUS (PROGUARD / R8)
# =============================================================================

# 1. Blindaje del Núcleo y Puentes de Privilegios (Shizuku)
# Evita que el optimizador altere o recorte las clases encargadas de la comunicación raíz
-keep class com.hypernexus.ShizukuBridge { *; }
-keep class com.hypernexus.PrivilegedActions { *; }

# 2. Preservación del Entorno Gráfico (UI) y Jetpack Compose
# Mantiene intactas las pantallas, componentes y estados mutables para evitar cierres en Release
-keep class com.hypernexus.ui.** { *; }

# 3. Supresión de Alertas de Compilación (Silenciar Advertencias Externas)
# Evita que el compilador se detenga por referencias faltantes en las librerías oficiales de Shizuku
-dontwarn rikka.shizuku.**
-dontwarn com.hypernexus.**

# 4. Reglas Genéricas de Estabilidad para Kotlin y Atributos Críticos
# Conserva anotaciones y firmas genéricas necesarias para la inyección de dependencias y reflexión
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

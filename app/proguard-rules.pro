# Reglas de estabilidad para HyperNexus
-keep class com.hypernexus.ShizukuBridge { *; }
-keep class com.hypernexus.PrivilegedActions { *; }
-keep class com.hypernexus.ui.** { *; }
-dontwarn rikka.shizuku.**
-dontwarn com.hypernexus.**

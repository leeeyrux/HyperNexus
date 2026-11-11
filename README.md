# 🌌 HyperNexus v1.8.0 - Optimización Android Simple y Potente

<div align="center">
  <img src="app/src/main/res/drawable/ic_launcher.png" width="120" height="120" alt="HyperNexus Logo" style="border-radius: 24px;"/>
  <br />
  <p><b>Desarrollado desde un móvil (Termux) por @leeeyrux</b></p>
</div>

---

## 🔍 ¿Qué es HyperNexus?
**HyperNexus** es una app de optimización y diagnóstico para Android que pone el control de tu móvil en tus manos, sin Root obligatorio. Une en una sola app (16 MB, ultra-ligera) lo que antes necesitabas 5 apps distintas para hacer.

Funciona en dos niveles:
- **Telemetría en vivo** (lectura local, sin permisos especiales)
- **Optimizaciones reales** (vía Shizuku/ADB, sin Root en móviles modernos)

---

## 🛠️ 7 Pestañas, Todo en una Navegación Flotante

### 1. 📊 Panel — Tu Móvil en Tiempo Real
Vista limpia, solo datos, cero botones que estorben.
- **CPU:** Frecuencia real de cada núcleo (8 núcleos)
- **Batería:** Salud, voltaje (mV), amperaje (mA), tecnología, capacidad real
- **Red:** Ping (ms), señal (dBm), tipo (Wi-Fi 2.4/5 GHz, 4G/5G)
- **Acciones rápidas:** Limpiar RAM, Gaming Boost, Optimizar Ping

### 2. ⚡ Rendimiento — Ajustes Seguros Paso a Paso
Cambias valores con chips discretos, nada de sliders que se pasan.
- **Hz de pantalla:** Auto / 60 / 90 / 120 / 144 Hz
- **DPI (tamaño de interfaz):** 320–500 en saltos de 20 (rango seguro)
- **Animaciones:** Transición, Duración, Ventana → 0x / 0.5x / 1x / 1.5x / 2x
- **Resolución:** Nativa / 1080p / 720p (base segura 20:9) / 540p
- **Filtro táctil (Input Lag Bypass):** Respuesta más rápida al tocar
- **Efecto Cristal (Glass/Blur):** *Desactívalo en gama baja para más FPS*
- **Perfil Extremo (un toque):** Apaga animaciones, máxima CPU, GPU directa

### 3. 🎮 Gráficos — Para Juegos Exigentes (COD, Free Fire, Brawl Stars…)
- **HUD Red Magic:** Flotante, tap para alternar FPS simple / barra completa [Hora | FPS | Ping | Temp | Batería]
- **Motor de renderizado:** Vulkan Booster / Vulkan / Vulkan Lite / OpenGL SkiaGL / OpenGL Lite
- **Composición:** CPU / GPU / Dinámica
- **Interruptores:** Gráficos Fluidos / Aceleración HW / Intervalo GPU / Control Térmico / Touch Optimizado / Booster SkiaGL / Booster Vulkan

### 4. 🛡️ Seguridad & Red — Limpieza y Privacidad
- **Anillo Booster (Cian):** Tap → limpia memoria (`pm trim-caches`) en ~1.8 s
- **Anillo Antivirus (Verde):** Escanea apps instaladas, muestra progreso real
- **Limpieza automática:** Si RAM libre baja del 15%, purga en segundo plano
- **Anti-Throttling (DoH):** DNS sobre HTTPS (puerto 443) para que el operador no estrangule
- **DNS Privado Rápido (1.1.1.1):** Un interruptor, acelera web y evita bloqueos básicos
- **VPN Gaming (Split Tunneling):** Solo el juego elegido va por el túnel (MTU 1400, DNS 1.1.1.1), el resto directo
- **Botones pie:** Limpiar RAM / Gaming Boost / Optimizar Ping

### 5. 🗂️ Módulos — Instala Tus Propios Scripts
- **Importar .sh:** Selector nativo desde `/sdcard/HyperNexus_Modulos/`
- **Gestión visual:** Cada módulo = tarjeta con switch, ver/ocultar salida, eliminar
- **8 Presets de fábrica:**
  1. FPS Stabilizer
  2. Touch Response Booster
  3. Thermal Throttling Bypass
  4. Network Ping Optimizer
  5. RAM Management Aggressive
  6. GPU Sustained Clocks
  7. Kernel Entropy Multiplier
  8. Audio Bitrate Enhancer

### 6. 💻 Terminal — ADB en el Móvil
- Consola real vía Shizuku, sin PC
- 10 presets listos (Doze Extremo, Deep Sleep, Trim Caché, Kill All, SurfaceFlinger, Rendimiento Máx, Compilación Speed, Logcat Crashes, Propiedades Térmicas)
- Favoritos (marcar/desmarcar con ★)
- Fondo Matrix 4% opacidad, solo gris neutro sobre negro

### 7. 🧪 Experimental — Lo Más Avanzado
- **Compilación PGO (8 hilos):** Binario AOT optimizado por perfil de velocidad para el juego elegido
- **Game Manager API:** FPS objetivo (60/90/120/144) + downscale opcional 0.8x
- **ZRAM Gamer:** Swappiness 130 → texturas en RAM física, apps inactivas a swap

---

## 🎨 Diseño: Minimalista, Oscuro, 60 Hz Garantizados
- **Navegación:** Cápsula flotante iOS-style, 7 iconos, scroll horizontal con física elástica
- **Estilo:** Monocromo Bold-White (selección = blanco puro + negrita, resto = gris tenue)
- **Tarjetas:** Glassmorphism opcional — **desactivado por defecto en gama baja** (ahorra GPU). Si lo activas: blur real (API 31+) o gradiente simulado (< API 31). Siempre con borde metálico fino.
- **Entrada escalonada:** 30 dp → 0, 600 ms, ease-out-quad, 100 ms entre tarjetas (bypass `animator_duration_scale`)
- **Listas:** Caché de iconos + claves fijas = scroll fluido tipo TikTok, sin recalcular animaciones

---

## ⚙️ Requisitos y Compatibilidad
- Android 8.0+ (API 26)
- **Shizuku** instalado y permiso concedido (la app te guía al abrir)
- Funciona en **cualquier móvil**: desde Helio G36 / Snapdragon 450 hasta flagship
- Tamaño APK ~16 MB, sin publicidad, sin trackers

---

## 📥 Descarga e Instalación
1. Instala **Shizuku** (Play Store / GitHub)
2. Da permiso Shizuku a HyperNexus (ADB o app Shizuku)
3. Abre HyperNexus → concede permiso cuando pida
4. Empieza a optimizar

---

## ⚠️ Aviso Importante
Las opciones avanzadas (DPI, resolución, governor, thermal, etc.) modifican comportamiento del sistema. **Lee los diálogos de advertencia ("Che Jorge el Curioso") antes de aceptar**. El creador no se hace responsable de mal uso.

---

## 🔗 Enlaces
- **Telegram:** https://t.me/Leeeyrux_Nexus
- **TikTok:** https://tiktok.com/@leeeyrux
- **Instagram:** https://instagram.com/leeeyru.btw
- **YouTube:** https://youtube.com/@leeeyrux

---

*HyperNexus es software libre y transparente. Úsalo con criterio.*
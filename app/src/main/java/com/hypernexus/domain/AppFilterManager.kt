package com.hypernexus.domain

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.hypernexus.ShizukuBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GameTargetApp(
    val packageName: String,
    val appName: String,
    val isGame: Boolean,
    val icon: ImageBitmap? = null
)

class AppFilterManager(
    private val context: Context,
    private val shizukuBridge: ShizukuBridge? = null
) {
    private val gameKeywords = listOf(
        "freefire", "max", "dts", "cod", "pubg", "supercell", "game", "gaming",
        "mobile", "garena", "riotgames", "epicgames", "ea", "roblox", "tencent",
        "genshin", "honkai", "craft", "brawl", "clash", "fifa", "warzone", "league"
    )

    suspend fun getInstalledUserApplications(): List<GameTargetApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val ownPackage = context.packageName
        val packageNames = mutableSetOf<String>()

        // 1. Consulta nativa ilimitada de launchers
        try {
            val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }
            resolveInfos.forEach { packageNames.add(it.activityInfo.packageName) }
        } catch (_: Exception) {}

        // 2. Consulta de respaldo privilegiada con Shizuku
        if (shizukuBridge != null && shizukuBridge.isConnected.value) {
            try {
                val shellOut = shizukuBridge.executeShellCommand("pm list packages -3")
                if (shellOut.first == 0) {
                    shellOut.second.lines().forEach { line ->
                        val clean = line.removePrefix("package:").trim()
                        if (clean.isNotBlank()) packageNames.add(clean)
                    }
                }
            } catch (_: Exception) {}
        }

        val resultList = mutableListOf<GameTargetApp>()

        for (pkg in packageNames) {
            if (pkg == ownPackage || pkg == "moe.shizuku.privileged.api") continue

            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val isUserInstalled = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                val appLabel = pm.getApplicationLabel(appInfo).toString()

                val isGameCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                } else {
                    @Suppress("DEPRECATION")
                    (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                }

                // Detección heurística por palabras clave
                val hasGameKeyword = gameKeywords.any { kw ->
                    pkg.contains(kw, ignoreCase = true) || appLabel.contains(kw, ignoreCase = true)
                }

                val isGame = isGameCategory || hasGameKeyword

                // Filtrar utilerías de servicios de Google que no sean juegos
                val isExcluded = (pkg == "com.android.vending" ||
                        pkg.startsWith("com.google.android.gms") ||
                        pkg.startsWith("com.google.android.feedback")) && !isGame

                if (isUserInstalled && !isExcluded) {
                    val iconDrawable = try { pm.getApplicationIcon(appInfo) } catch (_: Exception) { null }
                    val imageBitmap = drawableToImageBitmap(iconDrawable)

                    resultList.add(
                        GameTargetApp(
                            packageName = pkg,
                            appName = appLabel,
                            isGame = isGame,
                            icon = imageBitmap
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // ALGORITMO DE PRIORIZACIÓN: Todos los juegos arriba, apps generales debajo en orden alfabético
        resultList.sortWith(
            compareByDescending<GameTargetApp> { it.isGame }
                .thenBy { it.appName.lowercase() }
        )
        resultList
    }

    private fun drawableToImageBitmap(drawable: Drawable?): ImageBitmap? {
        if (drawable == null) return null
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(48, 128) else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(48, 128) else 96
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

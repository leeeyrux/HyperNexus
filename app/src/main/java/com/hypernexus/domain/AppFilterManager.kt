package com.hypernexus.domain

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
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
    // Palabras clave de publishers y franquicias gaming
    private val gameKeywords = listOf(
        "freefire", "max", "dts", "roblox", "cod", "pubg", 
        "supercell", "ea", "mojang", "activision", "garena", "tencent"
    )

    // BARRERA DE EXCLUSIÓN ESTRICTA: Expulsadas de Tier 1 de forma forzosa
    private val strictExclusionKeywords = listOf(
        "gob.bancodelbienestar", "bienestar", "banco", "chamba", "empleo", "security"
    )

    suspend fun getInstalledUserApplications(): List<GameTargetApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val ownPackage = context.packageName

        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(PackageManager.GET_META_DATA)
            }
        } catch (_: Exception) {
            emptyList()
        }

        val tier1Games = mutableListOf<GameTargetApp>()
        val tier2Utilities = mutableListOf<GameTargetApp>()

        for (pkg in packages) {
            val pkgName = pkg.packageName
            val appInfo = pkg.applicationInfo ?: continue

            // Excluir la app propia y Shizuku
            if (pkgName == ownPackage || pkgName == "moe.shizuku.privileged.api") continue

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            // Filtrar servicios base del sistema no actualizados
            if (isSystem && !isUpdatedSystem) continue

            val pkgLower = pkgName.lowercase()
            val appLabel = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { pkgName }
            val appLabelLower = appLabel.lowercase()

            // 1. Barrera de exclusión estricta
            val isForceExcluded = strictExclusionKeywords.any { exc ->
                pkgLower.contains(exc) || appLabelLower.contains(exc)
            }

            // 2. Detección por metadatos nativos
            val isGameMetadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                        (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            // 3. Detección por coincidencia de strings
            val matchesGameKeyword = gameKeywords.any { kw ->
                pkgLower.contains(kw) || appLabelLower.contains(kw)
            }

            val isTier1 = !isForceExcluded && (isGameMetadata || matchesGameKeyword)

            val iconDrawable = try { pm.getApplicationIcon(appInfo) } catch (_: Exception) { null }
            val imageBitmap = drawableToImageBitmap(iconDrawable)

            val appItem = GameTargetApp(
                packageName = pkgName,
                appName = appLabel,
                isGame = isTier1,
                icon = imageBitmap
            )

            if (isTier1) {
                tier1Games.add(appItem)
            } else {
                tier2Utilities.add(appItem)
            }
        }

        // Ordenamiento alfabético independiente
        tier1Games.sortBy { it.appName.lowercase() }
        tier2Utilities.sortBy { it.appName.lowercase() }

        // Fusión: Tier 1 (Juegos) arriba, Tier 2 (Utilidades) debajo
        tier1Games + tier2Utilities
    }

    private fun drawableToImageBitmap(drawable: Drawable?): ImageBitmap? {
        if (drawable == null) return null
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(48, 110) else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(48, 110) else 96
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

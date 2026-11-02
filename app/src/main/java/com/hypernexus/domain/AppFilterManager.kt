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
    // Palabras clave estrictas de títulos y publishers gaming reconocidos
    private val strictGameKeywords = listOf(
        "freefire", "max", "dts", "cod", "pubg", "supercell", 
        "activision", "garena", "tencent", "epicgames"
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

        val listAGames = mutableListOf<GameTargetApp>()
        val listBOtherApps = mutableListOf<GameTargetApp>()

        for (pkg in packages) {
            val pkgName = pkg.packageName
            val appInfo = pkg.applicationInfo ?: continue

            // Excluir la app propia y la API de Shizuku
            if (pkgName == ownPackage || pkgName == "moe.shizuku.privileged.api") continue

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            // Filtrar servicios internos que no sean de usuario ni actualizados
            if (isSystem && !isUpdatedSystem) continue

            val pkgLower = pkgName.lowercase()
            val appLabel = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { pkgName }

            // Validación rigurosa de categoría juego
            val isGameCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                        (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            val matchesKeyword = strictGameKeywords.any { kw -> pkgLower.contains(kw) }
            val isVerifiedGame = isGameCategory || matchesKeyword

            val iconDrawable = try { pm.getApplicationIcon(appInfo) } catch (_: Exception) { null }
            val imageBitmap = drawableToImageBitmap(iconDrawable)

            val appItem = GameTargetApp(
                packageName = pkgName,
                appName = appLabel,
                isGame = isVerifiedGame,
                icon = imageBitmap
            )

            if (isVerifiedGame) {
                listAGames.add(appItem)
            } else {
                listBOtherApps.add(appItem)
            }
        }

        // Ordenamiento alfabético individual
        listAGames.sortBy { it.appName.lowercase() }
        listBOtherApps.sortBy { it.appName.lowercase() }

        // Fusión: Lista A (Juegos) al tope absoluto, Lista B (Resto de apps) abajo
        listAGames + listBOtherApps
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

package com.hypernexus.domain

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GameTargetApp(
    val packageName: String,
    val appName: String,
    val isGame: Boolean
)

class AppFilterManager(private val context: Context) {

    suspend fun getInstalledUserApplications(): List<GameTargetApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, 0)
        }

        val ownPackage = context.packageName
        val uniqueApps = mutableMapOf<String, GameTargetApp>()

        for (resolveInfo in resolveInfos) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val pkgName = appInfo.packageName

            if (pkgName == ownPackage || pkgName == "moe.shizuku.privileged.api") {
                continue
            }

            val isUserInstalled = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            val isGameCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                        (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            // Filtrar utilerías básicas de Google/Android que tengan activity de launcher
            val isExcludedUtility = pkgName == "com.android.vending" ||
                    pkgName.startsWith("com.google.android.gms") ||
                    pkgName.startsWith("com.google.android.feedback")

            if ((isUserInstalled && !isExcludedUtility) || isGameCategory) {
                val appLabel = pm.getApplicationLabel(appInfo).toString()
                uniqueApps[pkgName] = GameTargetApp(
                    packageName = pkgName,
                    appName = appLabel,
                    isGame = isGameCategory
                )
            }
        }

        uniqueApps.values
            .sortedWith(compareByDescending<GameTargetApp> { it.isGame }.thenBy { it.appName.lowercase() })
    }
}

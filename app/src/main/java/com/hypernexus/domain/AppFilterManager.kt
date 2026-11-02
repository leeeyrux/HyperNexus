package com.hypernexus.domain

import android.content.Context
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
        val queryFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.GET_META_DATA
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_UNINSTALLED_PACKAGES or PackageManager.GET_META_DATA
        }

        val installedList = pm.getInstalledApplications(queryFlags)
        val ownPackage = context.packageName

        installedList
            .filter { app ->
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystem = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                val isEligible = (!isSystem || isUpdatedSystem)

                isEligible && app.packageName != ownPackage && app.packageName != "moe.shizuku.privileged.api"
            }
            .map { app ->
                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    app.category == ApplicationInfo.CATEGORY_GAME || (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                } else {
                    @Suppress("DEPRECATION")
                    (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                }

                GameTargetApp(
                    packageName = app.packageName,
                    appName = pm.getApplicationLabel(app).toString(),
                    isGame = isGame
                )
            }
            .sortedWith(compareByDescending<GameTargetApp> { it.isGame }.thenBy { it.appName.lowercase() })
    }
}

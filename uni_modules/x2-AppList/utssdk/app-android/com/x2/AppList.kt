package com.x2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class AppItem(
    val icon: String,
    val name: String,
    val packageName: String,
    val version: String,
    val path: String,
    val flags: Int,
    val size: Long
)

class AppInfoHelper {

    companion object {
        const val FILTER_ALL = "all"
        const val FILTER_USER = "user"
        const val FILTER_SYSTEM = "system"
    }

    /**
     * 1. 获取本机安装应用列表
     * filter:
     * - all: 全部应用
     * - user: 用户安装应用
     * - system: 系统应用
     * 默认全部
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledAppList(
        context: Context,
        filter: String? = FILTER_ALL
    ): List<AppItem> {
        val pm = context.packageManager
        val applications = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val realFilter = normalizeFilter(filter)

        return applications
            .asSequence()
            .filter { matchFilter(it, realFilter) }
            .mapNotNull { appInfo ->
                try {
                    buildAppItem(context, pm, appInfo)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
            .toList()
    }

    /**
     * 2. 搜索应用
     * keyword 同时匹配 应用名 和 包名
     * filter 默认全部
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun searchInstalledApps(
        context: Context,
        keyword: String,
        filter: String? = FILTER_ALL
    ): List<AppItem> {
        val realFilter = normalizeFilter(filter)

        if (keyword.isBlank()) {
            return getInstalledAppList(context, realFilter)
        }

        val lowerKeyword = keyword.trim().lowercase(Locale.getDefault())
        val pm = context.packageManager
        val applications = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        return applications
            .asSequence()
            .filter { matchFilter(it, realFilter) }
            .mapNotNull { appInfo ->
                try {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val packageName = appInfo.packageName

                    val matched = appName.lowercase(Locale.getDefault()).contains(lowerKeyword) ||
                            packageName.lowercase(Locale.getDefault()).contains(lowerKeyword)

                    if (!matched) return@mapNotNull null

                    buildAppItem(context, pm, appInfo)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
            .toList()
    }


    private fun normalizeFilter(filter: String?): String {
        return when (filter?.trim()?.lowercase(Locale.getDefault())) {
            FILTER_USER -> FILTER_USER
            FILTER_SYSTEM -> FILTER_SYSTEM
            else -> FILTER_ALL
        }
    }

    private fun buildAppItem(
        context: Context,
        pm: PackageManager,
        appInfo: ApplicationInfo
    ): AppItem {
        val packageName = appInfo.packageName
        val packageInfo = getPackageInfoCompat(pm, packageName)

        val appName = pm.getApplicationLabel(appInfo).toString()
        val versionName = packageInfo.versionName ?: ""
        val apkPath = appInfo.sourceDir ?: ""
        val flags = appInfo.flags
        val size = getFileSizeSafely(apkPath)
        val iconDrawable = pm.getApplicationIcon(appInfo)
        val iconPath = saveAppIconToCache(context, iconDrawable, packageName)

        return AppItem(
            icon = iconPath,
            name = appName,
            packageName = packageName,
            version = versionName,
            path = apkPath,
            flags = flags,
            size = size
        )
    }

    private fun matchFilter(
        appInfo: ApplicationInfo,
        filter: String
    ): Boolean {
        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        return when (filter) {
            FILTER_USER -> !isSystemApp
            FILTER_SYSTEM -> isSystemApp
            else -> true
        }
    }

    private fun getPackageInfoCompat(
        pm: PackageManager,
        packageName: String
    ): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, 0)
        }
    }

    private fun getFileSizeSafely(path: String): Long {
        return try {
            val file = File(path)
            if (file.exists()) file.length() else 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun saveAppIconToCache(
        context: Context,
        drawable: Drawable,
        packageName: String
    ): String {
        val iconDir = File(context.cacheDir, "app_icons")
        if (!iconDir.exists()) {
            iconDir.mkdirs()
        }

        val iconFile = File(iconDir, "$packageName.png")

        if (!iconFile.exists()) {
            val bitmap = drawableToBitmap(drawable)
            FileOutputStream(iconFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
        }

        return "file://${iconFile.absolutePath}"
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
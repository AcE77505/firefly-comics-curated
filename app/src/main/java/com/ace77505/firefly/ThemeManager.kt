package com.ace77505.firefly

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "app_theme"

    // 主题选项
    enum class ThemeMode(val value: Int) {
        FOLLOW_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES)
    }

    // 监听主题变化的接口
    interface ThemeChangeListener {
        fun onThemeChanged()
    }

    private var themeChangedListeners = mutableSetOf<ThemeChangeListener>()

    fun addThemeChangeListener(listener: ThemeChangeListener) {
        themeChangedListeners.add(listener)
    }

    fun removeThemeChangeListener(listener: ThemeChangeListener) {
        themeChangedListeners.remove(listener)
    }

    private fun notifyThemeChanged() {
        // 通知所有监听器主题已更改
        themeChangedListeners.forEach { listener ->
            listener.onThemeChanged()
        }
    }


    // 获取可用的主题选项
    fun getAvailableThemes(): List<Pair<String, ThemeMode>> {
        val themes = mutableListOf<Pair<String, ThemeMode>>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9.0+ 支持系统级深色模式
            themes.add("跟随系统" to ThemeMode.FOLLOW_SYSTEM)
        }

        themes.add("浅色" to ThemeMode.LIGHT)
        themes.add("深色" to ThemeMode.DARK)

        return themes
    }

    // 获取当前主题
    fun getCurrentTheme(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_THEME, null)

        return when (savedTheme) {
            "FOLLOW_SYSTEM" -> ThemeMode.FOLLOW_SYSTEM
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> {
                // 默认值：Android 8.0以下默认浅色，以上默认跟随系统
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ThemeMode.FOLLOW_SYSTEM
                } else {
                    ThemeMode.LIGHT
                }
            }
        }
    }

    // 应用主题（实时切换，不需要重启）
    fun applyTheme(context: Context, themeMode: ThemeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode.value)

        // 保存设置
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, themeMode.name).apply()

        // 通知所有监听者
        notifyThemeChanged()
    }



    // 初始化应用主题（在Activity的onCreate中调用）
    fun initTheme(context: Context) {
        val currentTheme = getCurrentTheme(context)
        AppCompatDelegate.setDefaultNightMode(currentTheme.value)
    }
}
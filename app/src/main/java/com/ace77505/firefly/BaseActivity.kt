package com.ace77505.firefly

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseActivity : AppCompatActivity(), ThemeManager.ThemeChangeListener {

    protected lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.initTheme(this)
        super.onCreate(savedInstanceState)
        ThemeManager.addThemeChangeListener(this)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initToolbar()
    }

    private fun initToolbar() {
        try {
            toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            applyToolbarStyle()
        } catch (_: Exception) {
            // 布局可能没有 toolbar，忽略
        }
    }

    protected fun setToolbarTitle(title: String, showBackButton: Boolean = false) {
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(showBackButton)
        supportActionBar?.setDisplayShowHomeEnabled(showBackButton)
    }

    private fun applyToolbarStyle() {
        val isDark = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val primaryColor = if (isDark)
            ContextCompat.getColor(this, R.color.md_theme_dark_primary)
        else
            ContextCompat.getColor(this, R.color.md_theme_light_primary)

        val onPrimaryColor = if (isDark)
            ContextCompat.getColor(this, R.color.md_theme_dark_onPrimary)
        else
            ContextCompat.getColor(this, R.color.md_theme_light_onPrimary)

        // 设置 Toolbar 颜色（如果存在）
        try {
            toolbar.setBackgroundColor(primaryColor)
            toolbar.setTitleTextColor(onPrimaryColor)
            toolbar.navigationIcon?.setTint(onPrimaryColor)
        } catch (_: UninitializedPropertyAccessException) {
            // toolbar 未初始化，忽略
        }

        // 设置状态栏背景色（仍然使用 window.statusBarColor，兼容性良好）
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        @Suppress("DEPRECATION")
        window.statusBarColor = primaryColor

        // 使用 WindowInsetsControllerCompat 设置状态栏图标为浅色或深色（替代 systemUiVisibility）
        // WindowCompat/WindowInsetsControllerCompat 会在不同 API 级别做兼容处理
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        // isAppearanceLightStatusBars = true => 图标为暗色（适用于浅底色）
        insetsController.isAppearanceLightStatusBars = !isDark
    }

    override fun onThemeChanged() {
        applyToolbarStyle()
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeThemeChangeListener(this)
    }
}

/* ThemeManager 保持不变 */
object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "app_theme"

    enum class ThemeMode(val value: Int) {
        FOLLOW_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES)
    }

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
        themeChangedListeners.forEach { listener ->
            try { listener.onThemeChanged() } catch (_: Exception) {}
        }
    }

    fun getAvailableThemes(): List<Pair<String, ThemeMode>> {
        val themes = mutableListOf<Pair<String, ThemeMode>>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            themes.add("跟随系统" to ThemeMode.FOLLOW_SYSTEM)
        }
        themes.add("浅色" to ThemeMode.LIGHT)
        themes.add("深色" to ThemeMode.DARK)
        return themes
    }

    fun getCurrentTheme(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_THEME, null)
        return when (savedTheme) {
            "FOLLOW_SYSTEM" -> ThemeMode.FOLLOW_SYSTEM
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ThemeMode.FOLLOW_SYSTEM else ThemeMode.LIGHT
        }
    }

    fun applyTheme(context: Context, themeMode: ThemeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode.value)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_THEME, themeMode.name) }
        notifyThemeChanged()
    }

    fun initTheme(context: Context) {
        val currentTheme = getCurrentTheme(context)
        AppCompatDelegate.setDefaultNightMode(currentTheme.value)
    }
}
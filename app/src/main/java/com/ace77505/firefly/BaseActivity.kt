package com.ace77505.firefly

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.google.android.material.appbar.MaterialToolbar

/**
 * BaseActivity 保留为基类，负责 Toolbar 和 主题监听（ThemeChangeListener）。
 * 同一文件内定义了 object ThemeManager（原 ThemeManager.kt 的功能被合并到这里的顶层 object）。
 *
 * 目的：把 Theme 管理与 BaseActivity 放在同一文件，减少文件数量并保持 API 不变（仍可通过 ThemeManager 调用）。
 */

abstract class BaseActivity : AppCompatActivity(), ThemeManager.ThemeChangeListener {

    protected lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        // 初始化主题（合并后的 ThemeManager 使用相同 API）
        ThemeManager.initTheme(this)
        super.onCreate(savedInstanceState)

        // 添加主题监听
        ThemeManager.addThemeChangeListener(this)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        // 初始化Toolbar（如果布局包含 toolbar）
        initToolbar()
    }

    private fun initToolbar() {
        try {
            toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            // 应用标题栏样式
            applyToolbarStyle()
        } catch (e: Exception) {
            // 如果布局没有 toolbar，则忽略
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
            getColor(R.color.md_theme_dark_primary)
        else
            getColor(R.color.md_theme_light_primary)

        val onPrimaryColor = if (isDark)
            getColor(R.color.md_theme_dark_onPrimary)
        else
            getColor(R.color.md_theme_light_onPrimary)

        // 设置Toolbar颜色（如果存在）
        try {
            toolbar.setBackgroundColor(primaryColor)
            toolbar.setTitleTextColor(onPrimaryColor)
            toolbar.navigationIcon?.setTint(onPrimaryColor)
        } catch (e: UninitializedPropertyAccessException) {
            // toolbar 未初始化，忽略
        }

        // 设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = primaryColor

        // 设置状态栏文字颜色
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            var flags = window.decorView.systemUiVisibility
            flags = if (!isDark) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            window.decorView.systemUiVisibility = flags
        }
    }

    override fun onThemeChanged() {
        // 主题改变时更新标题栏样式
        applyToolbarStyle()
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeThemeChangeListener(this)
    }
}

/**
 * 合并后的 ThemeManager（原 ThemeManager.kt 的功能），保留原 API：
 * - ThemeMode enum
 * - add/remove listener
 * - getAvailableThemes / getCurrentTheme / applyTheme / initTheme
 *
 * 仍然以 ThemeManager.<method> 形式使用，故其他代码不需大量改动。
 */
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
            try {
                listener.onThemeChanged()
            } catch (_: Exception) { }
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

    // 应用主题（实时切换）
    fun applyTheme(context: Context, themeMode: ThemeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode.value)

        // 保存设置
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_THEME, themeMode.name) }

        // 通知所有监听者
        notifyThemeChanged()
    }

    // 初始化应用主题（在Activity的onCreate中调用）
    fun initTheme(context: Context) {
        val currentTheme = getCurrentTheme(context)
        AppCompatDelegate.setDefaultNightMode(currentTheme.value)
    }
}
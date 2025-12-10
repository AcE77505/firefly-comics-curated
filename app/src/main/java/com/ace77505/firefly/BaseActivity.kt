package com.ace77505.firefly

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseActivity : AppCompatActivity(), ThemeManager.ThemeChangeListener {

    protected lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        // 初始化主题
        ThemeManager.initTheme(this)
        super.onCreate(savedInstanceState)

        // 添加主题监听
        ThemeManager.addThemeChangeListener(this)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        // 初始化Toolbar
        initToolbar()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 应用标题栏样式
        applyToolbarStyle()
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

        // 设置Toolbar颜色
        toolbar.setBackgroundColor(primaryColor)
        toolbar.setTitleTextColor(onPrimaryColor)

        // 设置返回按钮颜色
        toolbar.navigationIcon?.setTint(onPrimaryColor)

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
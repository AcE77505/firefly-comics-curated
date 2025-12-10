package com.ace77505.firefly

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

class SettingsActivity : BaseActivity() {

    private lateinit var themeSpinner: Spinner
    private lateinit var tvAppearance: TextView
    private var isInitialSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        initViews()
        setupToolbar()
        setupThemeSpinner()
    }

    private fun initViews() {
        themeSpinner = findViewById(R.id.themeSpinner)
        tvAppearance = findViewById(R.id.tvAppearance)
    }

    private fun setupToolbar() {
        // 使用BaseActivity的方法设置标题和返回按钮
        setToolbarTitle("设置", true)
    }

    private fun setupThemeSpinner() {
        // 获取可用的主题选项
        val availableThemes = ThemeManager.getAvailableThemes()
        val themeNames = availableThemes.map { it.first }
        val themeModes = availableThemes.map { it.second }

        // 设置Spinner适配器
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter

        // 设置当前选中的主题
        val currentTheme = ThemeManager.getCurrentTheme(this)
        val currentIndex = themeModes.indexOf(currentTheme)
        if (currentIndex >= 0) {
            themeSpinner.setSelection(currentIndex)
        }

        // 设置选择监听
        themeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                if (!isInitialSelection) {
                    val selectedTheme = themeModes[position]
                    if (selectedTheme != currentTheme) {
                        ThemeManager.applyTheme(this@SettingsActivity, selectedTheme)
                        // 主题切换后，整个Activity会重新创建
                    }
                }
                isInitialSelection = false
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onThemeChanged() {
        super.onThemeChanged() // 调用父类方法更新标题栏

        // SettingsActivity在主题切换后重新创建
        recreate()
    }
}
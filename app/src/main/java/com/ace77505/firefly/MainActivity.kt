package com.ace77505.firefly

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: DataAdapter
    private lateinit var filterDialogHelper: FilterDialogHelper

    // Views
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultCount: TextView
    private lateinit var btnFilter: Button
    private lateinit var btnClearAll: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupViewModel()
        setupToolbar()
        setupClickListeners()
        setupFilterDialogHelper()

        // 深色模式按钮颜色设置
        updateButtonColors()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvResultCount = findViewById(R.id.tvResultCount)
        btnFilter = findViewById(R.id.btnFilter)
        btnClearAll = findViewById(R.id.btnClearAll)
    }

    private fun setupRecyclerView() {
        adapter = DataAdapter(onItemClick = { data ->
            openUrlInBrowser(data.id)
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.filteredData.collectLatest { data ->
                adapter.updateData(data)
            }
        }

        lifecycleScope.launch {
            viewModel.resultCount.collectLatest { count ->
                tvResultCount.text = "共找到 $count 条结果"
            }
        }
    }

    private fun setupToolbar() {
        // 使用BaseActivity的方法设置标题
        setToolbarTitle(getString(R.string.app_name))
    }

    private fun setupClickListeners() {
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        btnClearAll.setOnClickListener {
            viewModel.clearFilters()
        }
    }

    private fun setupFilterDialogHelper() {
        filterDialogHelper = FilterDialogHelper(this) { newFilterState ->
            viewModel.updateFilterState(newFilterState)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                openSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        filterDialogHelper.showFilterDialog(viewModel.filterState.value)
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openUrlInBrowser(id: String) {
        try {
            val url = "https://jm18c-ghj.cc/album/$id"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "未找到可用的浏览器应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "打开链接失败", Toast.LENGTH_SHORT).show()
        }
    }

    // 更新按钮颜色（深色模式适配）
    private fun updateButtonColors() {
        val isNight = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        if (isNight) {
            val nightColor = getColor(R.color.md_theme_dark_onSurface)
            btnFilter.setTextColor(nightColor)
            btnClearAll.setTextColor(nightColor)
        } else {
            val lightColor = getColor(R.color.md_theme_light_onSurface)
            btnFilter.setTextColor(lightColor)
            btnClearAll.setTextColor(lightColor)
        }
    }

    override fun onThemeChanged() {
        super.onThemeChanged() // 调用父类方法更新标题栏
        updateButtonColors()
    }
}
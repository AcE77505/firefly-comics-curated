package com.ace77505.firefly

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import android.content.res.Configuration

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: DataAdapter
    private lateinit var filterDialogHelper: FilterDialogHelper

    // Views
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultCount: TextView
    private lateinit var toolbar: Toolbar
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

        // 暂时把设置按钮颜色代码放在MainActivity，后续需要移到别处
        val isNight = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
        if (isNight) {
            val nightColor = getColor(R.color.md_theme_dark_onSurface)
            btnFilter.setTextColor(nightColor)
            btnClearAll.setTextColor(nightColor)
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvResultCount = findViewById(R.id.tvResultCount)
        toolbar = findViewById(R.id.toolbar)
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
        // 现在 MainViewModel 有正确的构造函数
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
        setSupportActionBar(toolbar)
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

    private fun showFilterDialog() {
        filterDialogHelper.showFilterDialog(viewModel.filterState.value)
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
}
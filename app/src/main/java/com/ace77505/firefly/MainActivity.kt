// MainActivity.kt
package com.ace77505.firefly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: DataAdapter
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultCount: TextView

    // 当前筛选状态
    private var currentSearchText = ""
    private var currentSelectedRecommend: Set<Int>? = null
    private var currentSelectedFilter1: Set<Int>? = null
    private var currentSelectedFilter2: Set<String>? = null
    private var currentSelectedFilter3: Set<Int>? = null
    private var currentSelectedFilter4: Set<Int>? = null
    private var currentSelectedFilter5: Set<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView() // 先初始化RecyclerView和Adapter
        setupViewModel()    // 再设置ViewModel
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvResultCount = findViewById(R.id.tvResultCount)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.let {
            setSupportActionBar(it)
        }
    }

    private fun setupRecyclerView() {
        // 初始化adapter并设置点击监听器
        adapter = DataAdapter(onItemClick = { data ->
            openUrlInBrowser(data.id)
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun openUrlInBrowser(id: String) {
        try {
            val url = "https://jm18c-ghj.cc/album/$id"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            // 检查是否有浏览器可以处理这个Intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // 如果没有浏览器应用，显示提示
                Toast.makeText(this, "未找到可用的浏览器应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "打开链接失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        val repository = DataRepository(this)
        viewModel = MainViewModel(repository)

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.filteredData.collectLatest { data ->
                // 现在adapter已经初始化，可以安全调用
                adapter.updateData(data)
                tvResultCount.text = "共找到 ${data.size} 条结果"
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            showFilterDialog()
        }

        findViewById<Button>(R.id.btnClearAll).setOnClickListener {
            clearAllFilters()
        }
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("筛选条件")
            .create()

        setupFilterDialogViews(dialogView, dialog)
        dialog.show()
    }

    private fun setupFilterDialogViews(dialogView: View, dialog: AlertDialog) {
        val etSearch = dialogView.findViewById<TextInputEditText>(R.id.etSearch)
        etSearch.setText(currentSearchText)

        // 设置当前选中的筛选条件 - 明确指定泛型类型
        setChipSelection<Int>(dialogView, R.id.chipGroupRecommend, currentSelectedRecommend, mapOf(
            R.id.chipRecommendYes to 1,
            R.id.chipRecommendNo to 0
        ))

        setChipSelection<Int>(dialogView, R.id.chipGroupFilter1, currentSelectedFilter1, mapOf(
            R.id.chipFilter1Yes to 1,
            R.id.chipFilter1No to 0
        ))

        setChipSelection<String>(dialogView, R.id.chipGroupFilter2, currentSelectedFilter2, mapOf(
            R.id.chipFilter2Unknown to "-1",
            R.id.chipFilter2None to "0",
            R.id.chipFilter2Light to "1",
            R.id.chipFilter2Medium to "2",
            R.id.chipFilter2Heavy to "3",
            R.id.chipFilter2Complete to "4"
        ))

        setChipSelection<Int>(dialogView, R.id.chipGroupFilter3, currentSelectedFilter3, mapOf(
            R.id.chipFilter3Unknown to -1,
            R.id.chipFilter3Yes to 1,
            R.id.chipFilter3No to 0
        ))

        setChipSelection<Int>(dialogView, R.id.chipGroupFilter4, currentSelectedFilter4, mapOf(
            R.id.chipFilter4Unknown to -1,
            R.id.chipFilter4Yes to 1,
            R.id.chipFilter4No to 0
        ))

        setChipSelection<String>(dialogView, R.id.chipGroupFilter5, currentSelectedFilter5, mapOf(
            R.id.chipFilter5Unknown to "-1",
            R.id.chipFilter5Type1 to "1",
            R.id.chipFilter5Type2 to "2",
            R.id.chipFilter5Type3 to "3",
            R.id.chipFilter5Type4 to "4"
        ))

        // 清除按钮
        dialogView.findViewById<Button>(R.id.btnClear).setOnClickListener {
            clearDialogSelections(dialogView)
        }

        // 应用按钮
        dialogView.findViewById<Button>(R.id.btnApply).setOnClickListener {
            applyFiltersFromDialog(dialogView, etSearch.text.toString())
            dialog.dismiss()
        }
    }

    private fun <T> setChipSelection(
        dialogView: View,
        chipGroupId: Int,
        selectedValues: Set<T>?,
        chipMap: Map<Int, T>
    ) {
        val chipGroup = dialogView.findViewById<ChipGroup>(chipGroupId)
        selectedValues?.let { values ->
            for ((chipId, value) in chipMap) {
                val chip = dialogView.findViewById<Chip>(chipId)
                chip.isChecked = values.contains(value)
            }
        }
    }

    private fun clearDialogSelections(dialogView: View) {
        val chipGroupIds = listOf(
            R.id.chipGroupRecommend,
            R.id.chipGroupFilter1,
            R.id.chipGroupFilter2,
            R.id.chipGroupFilter3,
            R.id.chipGroupFilter4,
            R.id.chipGroupFilter5
        )

        chipGroupIds.forEach { chipGroupId ->
            val chipGroup = dialogView.findViewById<ChipGroup>(chipGroupId)
            chipGroup.clearCheck()
        }

        val etSearch = dialogView.findViewById<TextInputEditText>(R.id.etSearch)
        etSearch.setText("")
    }

    private fun applyFiltersFromDialog(dialogView: View, searchText: String) {
        currentSearchText = searchText

        currentSelectedRecommend = getSelectedChipValues<Int>(
            dialogView, R.id.chipGroupRecommend, mapOf(
                R.id.chipRecommendYes to 1,
                R.id.chipRecommendNo to 0
            )
        )

        currentSelectedFilter1 = getSelectedChipValues<Int>(
            dialogView, R.id.chipGroupFilter1, mapOf(
                R.id.chipFilter1Yes to 1,
                R.id.chipFilter1No to 0
            )
        )

        currentSelectedFilter2 = getSelectedChipValues<String>(
            dialogView, R.id.chipGroupFilter2, mapOf(
                R.id.chipFilter2Unknown to "-1",
                R.id.chipFilter2None to "0",
                R.id.chipFilter2Light to "1",
                R.id.chipFilter2Medium to "2",
                R.id.chipFilter2Heavy to "3",
                R.id.chipFilter2Complete to "4"
            )
        )

        currentSelectedFilter3 = getSelectedChipValues<Int>(
            dialogView, R.id.chipGroupFilter3, mapOf(
                R.id.chipFilter3Unknown to -1,
                R.id.chipFilter3Yes to 1,
                R.id.chipFilter3No to 0
            )
        )

        currentSelectedFilter4 = getSelectedChipValues<Int>(
            dialogView, R.id.chipGroupFilter4, mapOf(
                R.id.chipFilter4Unknown to -1,
                R.id.chipFilter4Yes to 1,
                R.id.chipFilter4No to 0
            )
        )

        currentSelectedFilter5 = getSelectedChipValues<String>(
            dialogView, R.id.chipGroupFilter5, mapOf(
                R.id.chipFilter5Unknown to "-1",
                R.id.chipFilter5Type1 to "1",
                R.id.chipFilter5Type2 to "2",
                R.id.chipFilter5Type3 to "3",
                R.id.chipFilter5Type4 to "4"
            )
        )

        viewModel.applyFilters(
            searchText = currentSearchText,
            selectedRecommend = currentSelectedRecommend,
            selectedFilter1 = currentSelectedFilter1,
            selectedFilter2 = currentSelectedFilter2,
            selectedFilter3 = currentSelectedFilter3,
            selectedFilter4 = currentSelectedFilter4,
            selectedFilter5 = currentSelectedFilter5
        )
    }

    private fun <T> getSelectedChipValues(
        dialogView: View,
        chipGroupId: Int,
        chipMap: Map<Int, T>
    ): Set<T>? {
        val selectedValues = mutableSetOf<T>()

        for ((chipId, value) in chipMap) {
            val chip = dialogView.findViewById<Chip>(chipId)
            if (chip.isChecked) {
                selectedValues.add(value)
            }
        }

        return if (selectedValues.isEmpty()) null else selectedValues
    }

    private fun clearAllFilters() {
        currentSearchText = ""
        currentSelectedRecommend = null
        currentSelectedFilter1 = null
        currentSelectedFilter2 = null
        currentSelectedFilter3 = null
        currentSelectedFilter4 = null
        currentSelectedFilter5 = null

        viewModel.clearFilters()
    }
}
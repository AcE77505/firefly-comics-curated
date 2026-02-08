package com.ace77505.firefly

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * MainViewModel.kt
 * - 包含 MainViewModel（保留）
 * - 同文件内合并了原 DataRepository（private class）与原 FilterState（data class）
 *
 * 这样做把与数据加载相关的实现保持在 ViewModel 内，减少文件数但职责仍清晰。
 */

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // 内部的轻量级 Repository（原 DataRepository）
    private class DataRepository(private val context: Context) {
        fun loadDataFromAssets(filename: String = "firefly.csv"): List<FilterData> {
            val dataList = mutableListOf<FilterData>()

            try {
                context.assets.open(filename).use { inputStream ->
                    // 一次性读取整个文件内容
                    val fileContent = inputStream.readBytes().toString(Charsets.UTF_8)

                    // 统一处理换行符
                    val normalizedContent = fileContent
                        .replace("\r\n", "\n")  // Windows -> Unix
                        .replace("\r", "\n")    // Old Mac -> Unix

                    // 按行分割
                    val lines = normalizedContent.split("\n")

                    var isFirstLine = true
                    for (line in lines) {
                        if (line.isBlank()) continue // 跳过空行

                        if (isFirstLine) {
                            isFirstLine = false
                            continue // 跳过标题行
                        }

                        val values = parseCSVLine(line)
                        if (values.size >= 10) {
                            val data = FilterData(
                                id = values[0].trim(),
                                title = values[1].trim(),
                                recommend = values[2].trim().toIntOrNull() ?: -1,
                                filter1 = values[3].trim().toIntOrNull() ?: -1,
                                filter2 = values[4].trim().ifEmpty { "-1" },
                                filter3 = values[5].trim().toIntOrNull() ?: -1,
                                filter4 = values[6].trim().toIntOrNull() ?: -1,
                                filter5 = values[7].trim().ifEmpty { "-1" },
                                updateDate = values[8].trim(),
                                source = values[9].trim()
                            )
                            dataList.add(data)
                        } else {
                            // 打印有问题的行以便调试
                            println("Invalid CSV line (expected 10 columns, got ${'$'}{values.size}): ${'$'}line")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 打印加载的数据数量用于调试
            println("Loaded ${'$'}{dataList.size} records from CSV")

            return dataList
        }

        private fun parseCSVLine(line: String): List<String> {
            val result = mutableListOf<String>()
            var current = StringBuilder()
            var inQuotes = false

            for (i in line.indices) {
                when (val c = line[i]) {
                    '"' -> {
                        inQuotes = !inQuotes
                        current.append(c) // 保留引号
                    }
                    ',' if !inQuotes -> {
                        result.add(current.toString())
                        current = StringBuilder()
                    }
                    else -> {
                        current.append(c)
                    }
                }
            }
            result.add(current.toString())
            return result
        }
    }

    // FilterState 合并进 ViewModel 文件
    data class FilterState(
        var searchText: String = "",
        var selectedRecommend: Set<Int>? = null,
        var selectedFilter1: Set<Int>? = null,
        var selectedFilter2: Set<String>? = null,
        var selectedFilter3: Set<Int>? = null,
        var selectedFilter4: Set<Int>? = null,
        var selectedFilter5: Set<String>? = null
    ) {
        fun clear() {
            searchText = ""
            selectedRecommend = null
            selectedFilter1 = null
            selectedFilter2 = null
            selectedFilter3 = null
            selectedFilter4 = null
            selectedFilter5 = null
        }

        fun copyFrom(other: FilterState) {
            searchText = other.searchText
            selectedRecommend = other.selectedRecommend
            selectedFilter1 = other.selectedFilter1
            selectedFilter2 = other.selectedFilter2
            selectedFilter3 = other.selectedFilter3
            selectedFilter4 = other.selectedFilter4
            selectedFilter5 = other.selectedFilter5
        }
    }

    private val repository = DataRepository(application.applicationContext)

    // 数据流
    private val _allData = MutableStateFlow<List<FilterData>>(emptyList())
    private val _filteredData = MutableStateFlow<List<FilterData>>(emptyList())
    val filteredData: StateFlow<List<FilterData>> = _filteredData

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 筛选状态
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    // 结果数量
    private val _resultCount = MutableStateFlow(0)
    val resultCount: StateFlow<Int> = _resultCount

    init {
        loadData()
        observeFilteredData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.loadDataFromAssets()
                _allData.value = data
                _filteredData.value = data // 初始显示所有数据
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeFilteredData() {
        viewModelScope.launch {
            _filteredData.collect { data ->
                _resultCount.value = data.size
            }
        }
    }

    fun updateFilterState(newState: FilterState) {
        _filterState.value = newState
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val state = _filterState.value
            val allData = _allData.value

            val filtered = allData.filter { data ->
                // 文本搜索
                val matchesSearch = state.searchText.isEmpty() ||
                        data.id.contains(state.searchText, ignoreCase = true) ||
                        data.title.contains(state.searchText, ignoreCase = true)

                // 筛选条件
                val matchesFilter = data.matchesFilter(
                    state.selectedRecommend,
                    state.selectedFilter1,
                    state.selectedFilter2,
                    state.selectedFilter3,
                    state.selectedFilter4,
                    state.selectedFilter5
                )

                matchesSearch && matchesFilter
            }

            _filteredData.value = filtered
        }
    }

    fun clearFilters() {
        _filterState.value = FilterState().also { /* reset */ }
        _filteredData.value = _allData.value
    }
}
// MainViewModel.kt
package com.ace77505.firefly

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

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
        _filterState.update { it.clear(); it }
        _filteredData.value = _allData.value
    }
}
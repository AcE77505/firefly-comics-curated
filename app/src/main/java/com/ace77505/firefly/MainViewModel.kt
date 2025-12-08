// MainViewModel.kt
package com.ace77505.firefly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DataRepository) : ViewModel() {

    private val _allData = MutableStateFlow<List<FilterData>>(emptyList())
    val allData: StateFlow<List<FilterData>> = _allData

    private val _filteredData = MutableStateFlow<List<FilterData>>(emptyList())
    val filteredData: StateFlow<List<FilterData>> = _filteredData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.loadDataFromAssets()
            _allData.value = data
            _filteredData.value = data
            _isLoading.value = false
        }
    }

    fun applyFilters(
        searchText: String = "",
        selectedRecommend: Set<Int>? = null,
        selectedFilter1: Set<Int>? = null,
        selectedFilter2: Set<String>? = null,
        selectedFilter3: Set<Int>? = null,
        selectedFilter4: Set<Int>? = null,
        selectedFilter5: Set<String>? = null
    ) {
        viewModelScope.launch {
            val filtered = _allData.value.filter { data ->
                // 文本搜索
                val matchesSearch = searchText.isEmpty() ||
                        data.id.contains(searchText, ignoreCase = true) ||
                        data.title.contains(searchText, ignoreCase = true)

                // 筛选条件
                val matchesFilter = data.matchesFilter(
                    selectedRecommend,
                    selectedFilter1,
                    selectedFilter2,
                    selectedFilter3,
                    selectedFilter4,
                    selectedFilter5
                )

                matchesSearch && matchesFilter
            }
            _filteredData.value = filtered
        }
    }

    fun clearFilters() {
        _filteredData.value = _allData.value
    }
}
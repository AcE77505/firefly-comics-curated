package com.ace77505.firefly

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
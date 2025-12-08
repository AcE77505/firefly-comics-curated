// DataModel.kt
package com.ace77505.firefly

data class FilterData(
    val id: String,
    val title: String,
    val recommend: Int,
    val filter1: Int,
    val filter2: String,
    val filter3: Int,
    val filter4: Int,
    val filter5: String,
    val updateDate: String,
    val source: String
) {
    fun matchesFilter(
        selectedRecommend: Set<Int>?,
        selectedFilter1: Set<Int>?,
        selectedFilter2: Set<String>?,
        selectedFilter3: Set<Int>?,
        selectedFilter4: Set<Int>?,
        selectedFilter5: Set<String>?
    ): Boolean {
        // 检查推荐
        selectedRecommend?.let {
            if (it.isNotEmpty() && !it.contains(recommend)) return false
        }

        // 检查筛选1
        selectedFilter1?.let {
            if (it.isNotEmpty() && !it.contains(filter1)) return false
        }

        // 检查筛选2（支持加号组合）
        selectedFilter2?.let {
            if (it.isNotEmpty()) {
                val filter2Values = filter2.split("+").map { value -> value.trim() }
                if (!filter2Values.any { value -> it.contains(value) }) return false
            }
        }

        // 检查筛选3
        selectedFilter3?.let {
            if (it.isNotEmpty() && !it.contains(filter3)) return false
        }

        // 检查筛选4
        selectedFilter4?.let {
            if (it.isNotEmpty() && !it.contains(filter4)) return false
        }

        // 检查筛选5（支持加号组合）
        selectedFilter5?.let {
            if (it.isNotEmpty()) {
                val filter5Values = filter5.split("+").map { value -> value.trim() }
                if (!filter5Values.any { value -> it.contains(value) }) return false
            }
        }

        return true
    }
}
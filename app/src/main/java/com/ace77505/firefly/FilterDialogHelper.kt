package com.ace77505.firefly

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class FilterDialogHelper(
    private val activity: AppCompatActivity,
    private val onApplyFilters: (FilterState) -> Unit
) {

    private var dialogView: View? = null
    private var dialog: AlertDialog? = null
    private var currentState = FilterState()

    fun showFilterDialog(initialState: FilterState) {
        currentState.copyFrom(initialState)

        val dialogView = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_filter, null)

        dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setTitle("筛选条件")
            .create()

        setupDialogViews(dialogView)
        dialog?.show()
    }

    private fun setupDialogViews(dialogView: View) {
        this.dialogView = dialogView

        // 设置搜索文本
        val etSearch = dialogView.findViewById<TextInputEditText>(R.id.etSearch)
        etSearch.setText(currentState.searchText)

        // 设置各个筛选器的选中状态
        setChipSelection(currentState.selectedRecommend, mapOf(
            R.id.chipRecommendYes to 1,
            R.id.chipRecommendNo to 0
        ))

        setChipSelection(currentState.selectedFilter1, mapOf(
            R.id.chipFilter1Yes to 1,
            R.id.chipFilter1No to 0
        ))

        setChipSelection(currentState.selectedFilter2, mapOf(
            R.id.chipFilter2Unknown to "-1",
            R.id.chipFilter2None to "0",
            R.id.chipFilter2Light to "1",
            R.id.chipFilter2Medium to "2",
            R.id.chipFilter2Heavy to "3",
            R.id.chipFilter2Complete to "4"
        ))

        setChipSelection(currentState.selectedFilter3, mapOf(
            R.id.chipFilter3Unknown to -1,
            R.id.chipFilter3Yes to 1,
            R.id.chipFilter3No to 0
        ))

        setChipSelection(currentState.selectedFilter4, mapOf(
            R.id.chipFilter4Unknown to -1,
            R.id.chipFilter4Yes to 1,
            R.id.chipFilter4No to 0
        ))

        setChipSelection(currentState.selectedFilter5, mapOf(
            R.id.chipFilter5Unknown to "-1",
            R.id.chipFilter5Type1 to "1",
            R.id.chipFilter5Type2 to "2",
            R.id.chipFilter5Type3 to "3",
            R.id.chipFilter5Type4 to "4"
        ))

        // 清除按钮
        dialogView.findViewById<android.widget.Button>(R.id.btnClear).setOnClickListener {
            clearDialogSelections()
        }

        // 应用按钮
        dialogView.findViewById<android.widget.Button>(R.id.btnApply).setOnClickListener {
            applyFilters()
            dialog?.dismiss()
        }
    }

    private fun <T> setChipSelection(selectedValues: Set<T>?, chipMap: Map<Int, T>) {
        selectedValues?.let { values ->
            for ((chipId, value) in chipMap) {
                val chip = dialogView?.findViewById<Chip>(chipId)
                chip?.isChecked = values.contains(value)
            }
        }
    }

    private fun clearDialogSelections() {
        val chipGroupIds = listOf(
            R.id.chipGroupRecommend,
            R.id.chipGroupFilter1,
            R.id.chipGroupFilter2,
            R.id.chipGroupFilter3,
            R.id.chipGroupFilter4,
            R.id.chipGroupFilter5
        )

        chipGroupIds.forEach { chipGroupId ->
            val chipGroup = dialogView?.findViewById<ChipGroup>(chipGroupId)
            chipGroup?.clearCheck()
        }

        val etSearch = dialogView?.findViewById<TextInputEditText>(R.id.etSearch)
        etSearch?.setText("")
    }

    private fun applyFilters() {
        val dialogView = this.dialogView ?: return

        // 更新当前状态
        currentState.searchText = dialogView.findViewById<TextInputEditText>(R.id.etSearch).text.toString()

        currentState.selectedRecommend = getSelectedChipValues(
            mapOf(
                R.id.chipRecommendYes to 1,
                R.id.chipRecommendNo to 0
            )
        )

        currentState.selectedFilter1 = getSelectedChipValues(
            mapOf(
                R.id.chipFilter1Yes to 1,
                R.id.chipFilter1No to 0
            )
        )

        currentState.selectedFilter2 = getSelectedChipValues(
            mapOf(
                R.id.chipFilter2Unknown to "-1",
                R.id.chipFilter2None to "0",
                R.id.chipFilter2Light to "1",
                R.id.chipFilter2Medium to "2",
                R.id.chipFilter2Heavy to "3",
                R.id.chipFilter2Complete to "4"
            )
        )

        currentState.selectedFilter3 = getSelectedChipValues(
            mapOf(
                R.id.chipFilter3Unknown to -1,
                R.id.chipFilter3Yes to 1,
                R.id.chipFilter3No to 0
            )
        )

        currentState.selectedFilter4 = getSelectedChipValues(
            mapOf(
                R.id.chipFilter4Unknown to -1,
                R.id.chipFilter4Yes to 1,
                R.id.chipFilter4No to 0
            )
        )

        currentState.selectedFilter5 = getSelectedChipValues(
            mapOf(
                R.id.chipFilter5Unknown to "-1",
                R.id.chipFilter5Type1 to "1",
                R.id.chipFilter5Type2 to "2",
                R.id.chipFilter5Type3 to "3",
                R.id.chipFilter5Type4 to "4"
            )
        )

        // 回调应用筛选
        onApplyFilters(currentState)
    }

    private fun <T> getSelectedChipValues(chipMap: Map<Int, T>): Set<T>? {
        val dialogView = this.dialogView ?: return null
        val selectedValues = mutableSetOf<T>()

        for ((chipId, value) in chipMap) {
            val chip = dialogView.findViewById<Chip>(chipId)
            if (chip?.isChecked == true) {
                selectedValues.add(value)
            }
        }

        return if (selectedValues.isEmpty()) null else selectedValues
    }
}
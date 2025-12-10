package com.ace77505.firefly

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

//形式逆天的史山代码，后期需要转移
class FilterDialogHelper(
    private val activity: AppCompatActivity,
    private val onApplyFilters: (FilterState) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var currentState = FilterState()

    // 缓存视图引用
    private lateinit var etSearch: TextInputEditText
    private val chipGroups = mutableMapOf<String, ChipGroup>()
    private val chipValueMaps = mutableMapOf<String, Map<Chip, Any>>()

    // 深色模式判断
    private val isDarkMode: Boolean by lazy {
        activity.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    // 动态获取颜色
    private fun getPrimaryColor(): Int {
        return if (isDarkMode) {
            activity.getColor(R.color.md_theme_dark_primary)
        } else {
            activity.getColor(R.color.md_theme_light_primary)
        }
    }

    private fun getOnPrimaryColor(): Int {
        return if (isDarkMode) {
            activity.getColor(R.color.md_theme_dark_onPrimary)
        } else {
            activity.getColor(R.color.md_theme_light_onPrimary)
        }
    }

    private fun getSurfaceColor(): Int {
        return if (isDarkMode) {
            activity.getColor(R.color.md_theme_dark_surface)
        } else {
            activity.getColor(R.color.md_theme_light_surface)
        }
    }

    private fun getOnSurfaceColor(): Int {
        return if (isDarkMode) {
            activity.getColor(R.color.md_theme_dark_onSurface)
        } else {
            activity.getColor(R.color.md_theme_light_onSurface)
        }
    }

    private fun getPrimaryContainerColor(): Int {
        return if (isDarkMode) {
            activity.getColor(R.color.md_theme_dark_primaryContainer)
        } else {
            activity.getColor(R.color.md_theme_light_primaryContainer)
        }
    }

    private fun getOnPrimaryContainerColor(): Int {
        return if (isDarkMode) {
            activity.getColor(R.color.md_theme_dark_onPrimaryContainer)
        } else {
            activity.getColor(R.color.md_theme_light_onPrimaryContainer)
        }
    }

    private fun getOutlineColor(): Int {
        return if (isDarkMode) {
            // 深色模式未选中：浅灰色边框
            0xFFAAAAAA.toInt()  // 或使用更亮的颜色
        } else {
            // 浅色模式未选中：主题主色
            getPrimaryColor()
        }
    }

    private fun getSelectedOutlineColor(): Int {
        return if (isDarkMode) {
            // 深色模式选中：使用明显的彩色边框
            // 选项1：亮蓝色（良好的对比度）
            0xFF4FC3F7.toInt()
            // 选项2：亮绿色
            // 0xFF69F0AE.toInt()
            // 选项3：亮橙色
            // 0xFFFF9800.toInt()
            // 选项4：更亮的紫色
            // getPrimaryColor().withAlpha(255) // 完全不透明
        } else {
            // 浅色模式选中：主题主色
            getPrimaryColor()
        }
    }

    fun showFilterDialog(initialState: FilterState) {
        currentState.copyFrom(initialState)

        if (dialog == null) {
            createDialog()
        } else {
            updateDialogState()
        }

        dialog?.show()
    }

    @SuppressLint("InflateParams")
    private fun createDialog() {
        // 使用代码构建整个对话框
        val scrollView = ScrollView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 20.dp, 24.dp, 20.dp)
            background = createSurfaceBackground()
        }

        // 1. 搜索框
        container.addView(createSearchField())

        // 添加垂直间隔
        container.addView(createSpacer(24.dp))

        // 2. 推荐筛选
        container.addView(createSectionTitle(activity.getString(R.string.recommended)))
        container.addView(createSpacer(8.dp))
        val recommendGroup = createChipGroup(
            mapOf(
                activity.getString(R.string.yes) to 1,
                activity.getString(R.string.no) to 0
            ),
            currentState.selectedRecommend,
            "recommend"
        )
        container.addView(recommendGroup)
        chipGroups["recommend"] = recommendGroup

        container.addView(createSpacer(20.dp))

        // 3. 筛选1
        container.addView(createSectionTitle(activity.getString(R.string.ai)))
        container.addView(createSpacer(8.dp))
        val filter1Group = createChipGroup(
            mapOf(
                activity.getString(R.string.yes) to 1,
                activity.getString(R.string.no) to 0
            ),
            currentState.selectedFilter1,
            "filter1"
        )
        container.addView(filter1Group)
        chipGroups["filter1"] = filter1Group

        container.addView(createSpacer(20.dp))

        // 4. 筛选2
        container.addView(createSectionTitle(activity.getString(R.string.censored_type)))
        container.addView(createSpacer(8.dp))
        val filter2Group = createChipGroup(
            mapOf(
                activity.getString(R.string.unknown) to "-1",
                activity.getString(R.string.uncensored) to "0",
                activity.getString(R.string.c_black_lines) to "1",
                activity.getString(R.string.c_thin_blur) to "2",
                activity.getString(R.string.c_thick_blur) to "3",
                activity.getString(R.string.white) to "4"
            ),
            currentState.selectedFilter2,
            "filter2"
        )
        container.addView(filter2Group)
        chipGroups["filter2"] = filter2Group

        container.addView(createSpacer(20.dp))

        // 5. 筛选3
        container.addView(createSectionTitle(activity.getString(R.string.full_color)))
        container.addView(createSpacer(8.dp))
        val filter3Group = createChipGroup(
            mapOf(
                activity.getString(R.string.unknown) to -1,
                activity.getString(R.string.yes) to 1,
                activity.getString(R.string.no) to 0
            ),
            currentState.selectedFilter3,
            "filter3"
        )
        container.addView(filter3Group)
        chipGroups["filter3"] = filter3Group

        container.addView(createSpacer(20.dp))

        // 6. 筛选4
        container.addView(createSectionTitle(activity.getString(R.string.chunai)))
        container.addView(createSpacer(8.dp))
        val filter4Group = createChipGroup(
            mapOf(
                activity.getString(R.string.unknown) to -1,
                activity.getString(R.string.yes) to 1,
                activity.getString(R.string.no) to 0
            ),
            currentState.selectedFilter4,
            "filter4"
        )
        container.addView(filter4Group)
        chipGroups["filter4"] = filter4Group

        container.addView(createSpacer(20.dp))

        // 7. 筛选5
        container.addView(createSectionTitle(activity.getString(R.string.tag)))
        container.addView(createSpacer(8.dp))
        val filter5Group = createChipGroup(
            mapOf(
                activity.getString(R.string.unknown) to "-1",
                activity.getString(R.string.tag_1p) to "1",
                activity.getString(R.string.tag_cealus) to "2",
                activity.getString(R.string.tag_futa) to "3",
                activity.getString(R.string.tag_3p_or_more) to "4"
            ),
            currentState.selectedFilter5,
            "filter5"
        )
        container.addView(filter5Group)
        chipGroups["filter5"] = filter5Group

        // 添加按钮前的大间隔
        container.addView(createSpacer(28.dp))

        // 8. 按钮
        container.addView(createButtons())

        scrollView.addView(container)

        dialog = AlertDialog.Builder(activity)
            .setTitle("筛选条件")
            .setView(scrollView)
            .create()

        // 设置对话框背景
        dialog?.window?.setBackgroundDrawable(createDialogBackground())
    }

    private fun createSearchField(): TextInputLayout {
        val textInputLayout = TextInputLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // 使用轮廓模式
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            hint = activity.getString(R.string.search_id_or_name)
            isHintEnabled = true

            // 设置圆角
            shapeAppearanceModel = ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.ROUNDED, 12.dp.toFloat())
                .build()
        }

        etSearch = TextInputEditText(activity).apply {
            setText(currentState.searchText)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // 添加内边距
            setPadding(16.dp, 14.dp, 16.dp, 14.dp)
        }

        textInputLayout.addView(etSearch)
        return textInputLayout
    }

    private fun createSectionTitle(text: String): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getOnSurfaceColor())
            alpha = 0.6f // 稍微透明，作为二级标题
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun <T> createChipGroup(
        chipLabelValueMap: Map<String, T>,
        selectedValues: Set<T>?,
        groupKey: String
    ): ChipGroup {
        return ChipGroup(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isSingleSelection = false
            chipSpacingHorizontal = 8.dp
            chipSpacingVertical = 8.dp

            val chipValueMap = mutableMapOf<Chip, Any>()

            chipLabelValueMap.forEach { (label, value) ->
                val chip = createChip(label, selectedValues?.contains(value) ?: false)
                addView(chip)
                chipValueMap[chip] = value as Any
            }

            chipValueMaps[groupKey] = chipValueMap
        }
    }

    private fun createChip(label: String, isInitiallyChecked: Boolean): Chip {
        return Chip(activity).apply {
            text = label
            isCheckable = true
            isChecked = isInitiallyChecked

            // 设置圆角
            shapeAppearanceModel = ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.ROUNDED, 8.dp.toFloat())
                .build()

            // 根据初始状态设置样式
            updateChipStyle(this, isInitiallyChecked)

            // 监听选中状态变化
            setOnCheckedChangeListener { _, isChecked ->
                updateChipStyle(this, isChecked)
            }

            layoutParams = ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.WRAP_CONTENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
            )

            // 添加内边距
            setPadding(12.dp, 8.dp, 12.dp, 8.dp)
        }
    }

    private fun updateChipStyle(chip: Chip, isChecked: Boolean) {
        if (isDarkMode) {
            // 深色模式样式 - 提高对比度
            if (isChecked) {
                // 选中：较深的紫色背景 + 白色文字 + 较亮的紫色边框（2dp）
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    getPrimaryColor().withAlpha(180) // 70% 不透明度的紫色
                )
                chip.setTextColor(getOnPrimaryColor())
                chip.chipStrokeColor = ColorStateList.valueOf(getSelectedOutlineColor())
                chip.chipStrokeWidth = 2.5f.dp // 更粗的边框

                // 添加轻微的内阴影效果
                chip.elevation = 2.dp.toFloat()
            } else {
                // 未选中：透明背景 + 白色文字 + 明显的浅灰色边框（1.5dp）
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(activity, android.R.color.transparent)
                )
                chip.setTextColor(getOnSurfaceColor())
                chip.chipStrokeColor = ColorStateList.valueOf(getOutlineColor())
                chip.chipStrokeWidth = 1.5f.dp // 稍粗的未选中边框
                chip.elevation = 0f
            }
        } else {
            // 浅色模式样式（保持不变）
            if (isChecked) {
                // 选中：primaryContainer 背景 + onPrimaryContainer 文字 + 2dp 紫色边框
                chip.chipBackgroundColor = ColorStateList.valueOf(getPrimaryContainerColor())
                chip.setTextColor(getOnPrimaryContainerColor())
                chip.chipStrokeColor = ColorStateList.valueOf(getSelectedOutlineColor())
                chip.chipStrokeWidth = 2.dp.toFloat()
                chip.elevation = 2.dp.toFloat()
            } else {
                // 未选中：透明背景 + 黑色文字 + 1dp 紫色边框
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(activity, android.R.color.transparent)
                )
                chip.setTextColor(getOnSurfaceColor())
                chip.chipStrokeColor = ColorStateList.valueOf(getOutlineColor())
                chip.chipStrokeWidth = 1.dp.toFloat()
                chip.elevation = 0f
            }
        }
    }

    private fun createButtons(): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val clearButton = MaterialButton(activity).apply {
                text = activity.getString(R.string.clear)

                // 轮廓按钮样式 - 深色模式下使用更明显的边框
                setTextColor(getOnSurfaceColor())
                strokeColor = ColorStateList.valueOf(getOutlineColor())
                strokeWidth = 2.dp // 稍粗的边框
                setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent))

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginEnd = 8.dp
                }

                cornerRadius = 20.dp
                setPadding(16.dp, 12.dp, 16.dp, 12.dp)

                setOnClickListener { clearDialogSelections() }
            }

            val applyButton = MaterialButton(activity).apply {
                text = activity.getString(R.string.apply)

                // 轮廓按钮样式 - 深色模式下使用更明显的边框
                setTextColor(getOnSurfaceColor())
                strokeColor = ColorStateList.valueOf(getOutlineColor())
                strokeWidth = 2.dp // 稍粗的边框
                setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent))

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginStart = 8.dp
                }

                cornerRadius = 20.dp
                setPadding(16.dp, 12.dp, 16.dp, 12.dp)

                setOnClickListener {
                    applyFilters()
                    dialog?.dismiss()
                }
            }

            addView(clearButton)
            addView(applyButton)
        }
    }

    private fun createSpacer(height: Int): android.view.View {
        return android.view.View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            )
        }
    }

    private fun createSurfaceBackground(): MaterialShapeDrawable {
        return MaterialShapeDrawable(
            ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.ROUNDED, 16.dp.toFloat())
                .build()
        ).apply {
            fillColor = ColorStateList.valueOf(getSurfaceColor())
        }
    }

    private fun createDialogBackground(): MaterialShapeDrawable {
        return MaterialShapeDrawable(
            ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.ROUNDED, 28.dp.toFloat())
                .build()
        ).apply {
            fillColor = ColorStateList.valueOf(getSurfaceColor())
        }
    }

    private fun updateDialogState() {
        etSearch.setText(currentState.searchText)

        updateChipGroupState("recommend", currentState.selectedRecommend)
        updateChipGroupState("filter1", currentState.selectedFilter1)
        updateChipGroupState("filter2", currentState.selectedFilter2)
        updateChipGroupState("filter3", currentState.selectedFilter3)
        updateChipGroupState("filter4", currentState.selectedFilter4)
        updateChipGroupState("filter5", currentState.selectedFilter5)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> updateChipGroupState(groupKey: String, selectedValues: Set<T>?) {
        val chipMap = chipValueMaps[groupKey] ?: return
        chipMap.forEach { (chip, rawValue) ->
            val value = rawValue as? T
            val shouldBeChecked = selectedValues?.contains(value) ?: false
            chip.isChecked = shouldBeChecked
            updateChipStyle(chip, shouldBeChecked)
        }
    }

    private fun clearDialogSelections() {
        chipGroups.values.forEach { it.clearCheck() }
        chipValueMaps.values.forEach { chipMap ->
            chipMap.keys.forEach { chip ->
                chip.isChecked = false
                updateChipStyle(chip, false)
            }
        }
        etSearch.setText("")
    }

    private fun applyFilters() {
        currentState.searchText = etSearch.text.toString()

        currentState.selectedRecommend = getSelectedValuesAsInt("recommend")
        currentState.selectedFilter1 = getSelectedValuesAsInt("filter1")
        currentState.selectedFilter2 = getSelectedValuesAsString("filter2")
        currentState.selectedFilter3 = getSelectedValuesAsInt("filter3")
        currentState.selectedFilter4 = getSelectedValuesAsInt("filter4")
        currentState.selectedFilter5 = getSelectedValuesAsString("filter5")

        onApplyFilters(currentState)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSelectedValuesAsInt(groupKey: String): Set<Int>? {
        val chipMap = chipValueMaps[groupKey] ?: return null
        val selectedValues = mutableSetOf<Int>()

        chipMap.forEach { (chip, rawValue) ->
            if (chip.isChecked) {
                when (rawValue) {
                    is Int -> selectedValues.add(rawValue)
                    is String -> rawValue.toIntOrNull()?.let { selectedValues.add(it) }
                }
            }
        }

        return if (selectedValues.isEmpty()) null else selectedValues
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSelectedValuesAsString(groupKey: String): Set<String>? {
        val chipMap = chipValueMaps[groupKey] ?: return null
        val selectedValues = mutableSetOf<String>()

        chipMap.forEach { (chip, rawValue) ->
            if (chip.isChecked) {
                when (rawValue) {
                    is String -> selectedValues.add(rawValue)
                    is Int -> selectedValues.add(rawValue.toString())
                    else -> rawValue.toString().let { selectedValues.add(it) }
                }
            }
        }

        return if (selectedValues.isEmpty()) null else selectedValues
    }

    private val Int.dp: Int
        @SuppressLint("DiscouragedApi")
        get() = (this * activity.resources.displayMetrics.density).toInt()

    private val Float.dp: Float
        @SuppressLint("DiscouragedApi")
        get() = this * activity.resources.displayMetrics.density

    // 颜色透明度扩展函数
    private fun Int.withAlpha(alpha: Int): Int {
        return (alpha shl 24) or (this and 0x00FFFFFF)
    }
}